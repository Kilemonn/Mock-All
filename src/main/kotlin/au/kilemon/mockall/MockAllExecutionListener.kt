package au.kilemon.mockall

import jakarta.annotation.Resource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.Ordered
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Constructor
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

/**
 * A special [TestExecutionListener] that will [Mockito.mock] ALL injected beans in the context.
 * Except the beans marked as [NotMocked], or if they are in the [NotMocked.spyClasses] list they will be created as [Mockito.spy] objects.
 *
 * @author github.com/Kilemonn
 */
class MockAllExecutionListener : TestExecutionListener, Ordered
{
    companion object
    {
        const val ORDER = 10000

        private val initialisedMocks = HashMap<Class<*>, Any>()

        /**
         * Gets the existing instance for the provided [Class] from the [initialisedMocks] [HashMap].
         *
         * @param clazz the instance type to retrieve from the map
         * @return the existing object if it exists otherwise <code>null</code>
         */
        fun getInstance(clazz: Class<*>): Any?
        {
            return initialisedMocks[clazz]
        }

        /**
         * Checks if there is any child instances of the provided [Class] already defined in the [initialisedMocks].
         *
         * @param incomingClazz the provided base or intermediate [Class] to compare against the map to see if any child instances exist
         * @return the found child [Class] if one matches otherwise <code>null</code>
         */
        fun containsChildClass(incomingClazz: Class<*>): Class<*>?
        {
            val childClass = initialisedMocks.keys.stream().filter { clazz -> incomingClazz.isAssignableFrom(clazz) }.findFirst()
            return childClass.getOrNull()
        }

        /**
         * Clears the underlying [initialisedMocks] using [HashMap.clear].
         */
        fun clearInitialisedMocks()
        {
            initialisedMocks.clear()
        }
    }

    private val injectableAnnotation: List<Class<out Annotation>> = injectableAnnotations()

    private val spyKClasses: HashSet<KClass<*>> = HashSet()

    private lateinit var testContext: TestContext

    override fun getOrder(): Int
    {
        return ORDER
    }

    /**
     * Returns the held [testContext], this method is here to be used for mocking.
     */
    private fun getTestContext(): TestContext
    {
        return testContext
    }

    /**
     * Set the stored reference to the [TestContext]
     *
     * @param testContext the new [TestContext] to set
     */
    private fun setTestContext(testContext: TestContext)
    {
        this.testContext = testContext
    }

    /**
     * The entry point for the test instance initialisation.
     * This will iterate over all fields in the test class and inject any mocks required. This will inject mocks into
     * parent class members.
     *
     * @param testContext the [TestContext]
     */
    override fun prepareTestInstance(testContext: TestContext)
    {
        setTestContext(testContext)
        initialisedMocks[getTestContext().testClass] = getTestContext().testInstance
        val clazz = getTestContext().testClass
        mockAnnotationFields(clazz)
    }

    /**
     * Add the provided [KClass] to the underlying [spyKClasses] [Set].
     *
     * @param kClazz the [KClass] to add to the set
     */
    fun addToSpyClasses(kClazz: KClass<*>)
    {
        spyKClasses.add(kClazz)
    }

    /**
     * Checks if the provided [KClass] exists already in the [spyKClasses] [Set].
     *
     * @param kClazz the [KClass] to look up in the set
     * @return <code>true</code> if the class exists, otherwise <code>false</code>
     */
    fun containsKClasses(kClazz: KClass<*>): Boolean
    {
        return spyKClasses.contains(kClazz)
    }

    /**
     * This method will create a [Mockito.mock] of any field marked with an [injectableAnnotations].
     * Or, if the field is contained in [spyKClasses] it will be created as a [Mockito.spy].
     * The [NotMocked] class will be instantiated using a first constructor found.
     *
     * @param clazz the current class that we should process
     */
    private fun mockAnnotationFields(clazz: Class<*>)
    {
        var currentClazz = clazz
        do
        {
            for (field in currentClazz.declaredFields)
            {
                ReflectionUtils.makeAccessible(field)

                val notMocked: NotMocked? = field.getAnnotation(NotMocked::class.java)
                if (notMocked != null)
                {
                    spyKClasses.addAll(notMocked.spyClasses.asList())

                    val qualifier = field.getAnnotation(Qualifier::class.java)
                    ReflectionUtils.setField(field, createOrGetInstance(currentClazz), createActualOrSpy(field.type.kotlin, qualifier))
                    mockAnnotationFields(field.type)
                }
                else
                {
                    val hasAnyInjectionAnnotations = injectableAnnotation.stream().map { annotation -> field.getAnnotation(annotation) }.toList().filterNotNull().isNotEmpty()
                    if (hasAnyInjectionAnnotations)
                    {
                        ReflectionUtils.setField(field, createOrGetInstance(currentClazz), createOrGetInstance(field.type, spyKClasses.contains(field.type.kotlin)))
                        mockAnnotationFields(field.type)
                    }
                }
            }
            currentClazz = currentClazz.superclass
        } while (currentClazz != Any::class.java)
    }

    /**
     * Create an actual object of [T] or a [Mockito.spy] depending on whether the provided [KClass] exists in the [spyKClasses].
     * If we need to create an actual object we will delegate the creation to the [findZeroArgConstructor] to find the correct constructor.
     *
     * @param kClass the incoming class that we should create an instance or [Mockito.spy] of
     * @return the constructed [Mockito.spy] OR instance of [T]
     */
    fun <T : Any> createActualOrSpy(kClass: KClass<T>, qualifier: Qualifier? = null): T
    {
        val shouldSpyNotMocked = spyKClasses.contains(kClass)
        return if (shouldSpyNotMocked)
        {
            createOrGetInstance(kClass.java, true)
        }
        else
        {
            var instance = if (qualifier != null)
            {
                getBeanByName(qualifier.value, kClass)
            }
            else
            {
                getExistingBean(kClass)
            }

            if (instance == null)
            {
                instance = findZeroArgConstructor(kClass.java).newInstance()
            }
            instance!!
        }
    }

    /**
     * Find the [clazz]'s zero arg constructor.
     *
     * @param clazz the [Class] to find the zero arg constructor of
     * @return the zero argument constructor for the provided [Class]
     * @throws IllegalArgumentException if there is no zero argument constructor defined for this [clazz]
     */
    fun <T> findZeroArgConstructor(clazz: Class<T>): Constructor<T>
    {
        val constructors: Array<out Constructor<*>> = clazz.declaredConstructors
        val defaultConstructor = Arrays.stream(constructors).filter { constructor -> constructor.parameterCount == 0 }.findFirst()

        if (defaultConstructor.isEmpty)
        {
            throw IllegalArgumentException("Unable to find default zero argument constructor for class [" + clazz.name + "] for usage with [" + NotMocked::class.qualifiedName + "].")
        }

        return defaultConstructor.get() as Constructor<T>
    }

    /**
     * Create the provided [Class] of type [T] instance as either a [Mockito.mock] or [Mockito.spy] based on the provided [createSpy].
     *
     * @param clazz the class to create a [Mockito.mock] or [Mockito.spy] of
     * @param createSpy *true* to create a [Mockito.spy], otherwise will create [Mockito.mock]
     * @return the created [Mockito.mock] or [Mockito.spy] of type [T]
     */
    fun <T> createMockOrSpy(clazz: Class<T>, createSpy: Boolean = false): T
    {
        return if (createSpy)
        {
            Mockito.spy(clazz)
        }
        else
        {
            Mockito.mock(clazz)
        }
    }

    /**
     * Create the provided [Class] T instance as either a [Mockito.mock] or a [Mockito.spy].
     */
    fun <T : Any> createOrGetInstance(clazz: Class<T>, createSpy: Boolean = false): T
    {
        // If a child class is already initialised we should use that instead of re-creating a parent version of a class
        val childClass = containsChildClass(clazz)
        return if (childClass != null)
        {
            initialisedMocks[childClass] as T
        }
        else
        {
            val created = createMockOrSpy(clazz, createSpy)
            initialisedMocks[clazz] = created
            created
        }
    }

    /**
     * Return a list of Injectable [Annotation]s. If any member field has any of these, we will attempt to mock it.
     *
     * @return [List] of [Annotation] that should be injected with a mock
     */
    fun injectableAnnotations(): List<Class<out Annotation>>
    {
        return listOf(Autowired::class.java, Resource::class.java, javax.annotation.Resource::class.java)
    }

    private fun <T : Any> getExistingBean(kClass: KClass<T>): T?
    {
        return try
        {
            testContext.applicationContext.getBean(kClass.java)
        }
        catch (ex: Exception)
        {
            null
        }
    }

    private fun <T : Any> getBeanByName(name: String, kClass: KClass<T>): T?
    {
        return try
        {
            testContext.applicationContext.getBean(name, kClass.java)
        }
        catch (ex: Exception)
        {
            null
        }
    }
}
