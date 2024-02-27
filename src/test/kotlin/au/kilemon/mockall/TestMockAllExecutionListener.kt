package au.kilemon.mockall

import au.kilemon.mockall.models.BaseClass
import au.kilemon.mockall.models.ChildClass
import au.kilemon.mockall.models.ChildChildClass
import au.kilemon.mockall.models.NotMockedSpy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestContext
import org.springframework.util.ReflectionUtils
import java.util.*
import javax.annotation.Resource

/**
 * Test class for [MockAllExecutionListener].
 *
 * @author github.com/Kilemonn
 */
class TestMockAllExecutionListener
{
    /**
     * Setup before each method.
     * Clearing the initialised mocks static map.
     */
    @BeforeEach
    fun setUp()
    {
        MockAllExecutionListener.clearInitialisedMocks()
    }

    /**
     * Ensure when [MockAllExecutionListener.prepareTestInstance] is called with a base class that the appropriate properties are mocked and available in the map.
     */
    @Test
    fun testPrepareTestInstance_onBaseClass()
    {
        MockAllExecutionListener().prepareTestInstance(initialiseContextWithInstance(BaseClass()))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(BaseClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Properties::class.java))
    }

    /**
     * Ensure when [MockAllExecutionListener.prepareTestInstance] is called with a child class that the child and parent injected properties are mocked and available in the map.
     */
    @Test
    fun testPrepareTestInstance_onChildClass()
    {
        MockAllExecutionListener().prepareTestInstance(initialiseContextWithInstance(ChildClass()))

        Assertions.assertNull(MockAllExecutionListener.getInstance(BaseClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Properties::class.java))

        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Module::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Package::class.java))
    }

    /**
     * A helper method to initialise a [TestContext] with the provided [instance] [T].
     *
     * @param instance object of type that will be placed into the mock [TestContext] with its type
     * @return a mock [TestContext] with the provided object as the context object
     */
    private fun <T: Any>initialiseContextWithInstance(instance: T): TestContext
    {
        val context: TestContext = Mockito.mock(TestContext::class.java)
        Mockito.`when`(context.testClass).thenReturn(instance::class.java)
        Mockito.`when`(context.testInstance).thenReturn(instance)
        return context
    }

    /**
     * Ensure [MockAllExecutionListener.createMockOrSpy] creates a mock or spy object as noted in the arguments.
     */
    @Test
    fun testCreateMockOrSpy()
    {
        var properties: Properties = MockAllExecutionListener().createMockOrSpy(Properties::class.java)
        Assertions.assertTrue(isMock(properties))

        properties = MockAllExecutionListener().createMockOrSpy(Properties::class.java, false)
        Assertions.assertTrue(isMock(properties))

        properties = MockAllExecutionListener().createMockOrSpy(Properties::class.java, true)
        Assertions.assertTrue(isSpy(properties))
    }

    /**
     * Ensure [MockAllExecutionListener.createActualOrSpy] creates an instance object when it's not in the spy class list.
     */
    @Test
    fun testCreateActualOrSpy_createInstance()
    {
        val properties: Properties = MockAllExecutionListener().createActualOrSpy(Properties::class)
        Assertions.assertFalse(isSpy(properties))
    }

    /**
     * Ensure [MockAllExecutionListener.createActualOrSpy] creates a spy object when it's in the spy class list.
     */
    @Test
    fun testCreateActualOrSpy_createSpy()
    {
        val listener = MockAllExecutionListener()
        listener.addToSpyClasses(Properties::class)

        val properties: Properties = listener.createActualOrSpy(Properties::class)
        Assertions.assertTrue(isSpy(properties))
    }

    /**
     * Ensure [MockAllExecutionListener.createOrGetInstance] creates a new instance of an object if it does not already exist.
     * And once created it will return the same object if the same type is provided.
     */
    @Test
    fun testCreateOrGetInstance_createNewInstance()
    {
        Assertions.assertNull(MockAllExecutionListener.getInstance(Properties::class.java))
        val properties = MockAllExecutionListener().createOrGetInstance(Properties::class.java, false)
        Assertions.assertNotNull(properties)

        val lookedUp = MockAllExecutionListener.getInstance(Properties::class.java)
        Assertions.assertNotNull(lookedUp)

        Assertions.assertEquals(properties, lookedUp)
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns the appropriate class when a child class for the
     * argument does exist in the map already.
     */
    @Test
    fun testContainsChildClass_childClassExists()
    {
        MockAllExecutionListener().createOrGetInstance(ChildClass::class.java, false)
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertEquals(ChildClass::class.java, MockAllExecutionListener.containsChildClass(BaseClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns <code>null</code> when there are no related objects
     * stored in the map.
     */
    @Test
    fun testContainsChildClass_noObjectsCreated()
    {
        Assertions.assertNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertNull(MockAllExecutionListener.containsChildClass(BaseClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns the base class, when only the base class is in the map
     * with none of its children classes.
     */
    @Test
    fun testContainsChildClass_parentCreatedLookedUpByParent()
    {
        MockAllExecutionListener().createOrGetInstance(BaseClass::class.java, false)
        Assertions.assertNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertEquals(BaseClass::class.java, MockAllExecutionListener.containsChildClass(BaseClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns <code>null</code> when a child class is used and its
     * parent exists in the map.
     */
    @Test
    fun testContainsChildClass_parentCreatedLookedUpByChild()
    {
        MockAllExecutionListener().createOrGetInstance(BaseClass::class.java, false)
        Assertions.assertNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertNull(MockAllExecutionListener.containsChildClass(ChildClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns the appropriate child class when the child class
     * exists in the map but the base class does not and the base class is used for the look-up.
     */
    @Test
    fun testContainsChildClass_childCreatedLookedUpByParent()
    {
        MockAllExecutionListener().createOrGetInstance(ChildClass::class.java, false)
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertEquals(ChildClass::class.java, MockAllExecutionListener.containsChildClass(BaseClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns the child class, when only the child class exists
     * and the child class is used for the look-up.
     */
    @Test
    fun testContainsChildClass_childCreatedLookedUpByChild()
    {
        MockAllExecutionListener().createOrGetInstance(ChildClass::class.java, false)
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        Assertions.assertEquals(ChildClass::class.java, MockAllExecutionListener.containsChildClass(ChildClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.containsChildClass] returns a random class in the hierarchy when BOTH the parent
     * and child class exists and the parent is used for a look-up.
     * This behaviour would remain similar where the retrieved entities would not be guaranteed if there are multiple that
     * are children or equal to the requested object.
     * You should always use the lowest level object possible in the hierarchy to get consistent results.
     */
    @Test
    fun testContainsChildClass_bothCreatedLookedUpByBoth()
    {
        MockAllExecutionListener().createOrGetInstance(BaseClass::class.java, false)
        MockAllExecutionListener().createOrGetInstance(ChildClass::class.java, false)
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildClass::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(BaseClass::class.java))

        val parentLookUp = MockAllExecutionListener.containsChildClass(BaseClass::class.java)
        Assertions.assertTrue(parentLookUp == BaseClass::class.java || parentLookUp == ChildClass::class.java)

        Assertions.assertEquals(ChildClass::class.java, MockAllExecutionListener.containsChildClass(ChildClass::class.java))
    }

    /**
     * Ensure [MockAllExecutionListener.getOrder] returns [MockAllExecutionListener.ORDER].
     */
    @Test
    fun testGetOrder()
    {
        Assertions.assertEquals(MockAllExecutionListener.ORDER, MockAllExecutionListener().order)
    }

    /**
     * Ensure [MockAllExecutionListener.findZeroArgConstructor] throws an [IllegalArgumentException] when there are no
     * zero arg constructors available for the provided class.
     */
    @Test
    fun testFindZeroArgConstructor_noZeroArg()
    {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            MockAllExecutionListener().findZeroArgConstructor(Module::class.java)
        }
    }

    /**
     * Ensure [MockAllExecutionListener.findZeroArgConstructor] returns a non-null constructor and the object is constructed
     * successfully.
     */
    @Test
    fun testFindZeroArgConstructor_hasZeroArg()
    {
        val constructor = MockAllExecutionListener().findZeroArgConstructor(Properties::class.java)
        Assertions.assertNotNull(constructor)
        val instance = constructor.newInstance()
        Assertions.assertNotNull(instance)
    }

    /**
     * Ensure that when we use [NotMocked] that the annotated property is not available in the map. And that any specified
     * classes are actually created as a spy instead of a mock.
     */
    @Test
    fun testNotMocked()
    {
        val listener = MockAllExecutionListener()
        val thirdLevelClass = ChildChildClass()
        listener.prepareTestInstance(initialiseContextWithInstance(thirdLevelClass))

        Assertions.assertNotNull(MockAllExecutionListener.getInstance(ChildChildClass::class.java))

        // NotMocked class should not be initialised
        Assertions.assertNull(MockAllExecutionListener.getInstance(Thread::class.java))

        // Check the spy is initialised correctly
        Assertions.assertTrue(listener.containsKClasses(Properties::class))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Properties::class.java))
        Assertions.assertTrue(isSpy(MockAllExecutionListener.getInstance(Properties::class.java)!!))
    }

    /**
     * Ensure that when we use [NotMocked] with the provided property class as an argument that it is constructed and spied.
     */
    @Test
    fun testNotMocked_SpyNotMocked()
    {
        val listener = MockAllExecutionListener()
        val notMockedSpy = NotMockedSpy()
        listener.prepareTestInstance(initialiseContextWithInstance(notMockedSpy))

        Assertions.assertNotNull(MockAllExecutionListener.getInstance(NotMockedSpy::class.java))
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(Properties::class.java))
        Assertions.assertTrue(isSpy(MockAllExecutionListener.getInstance(Properties::class.java)!!))
        Assertions.assertNotNull(notMockedSpy.properties)
        Assertions.assertTrue(isSpy(notMockedSpy.properties))
    }

    /**
     * Ensure [MockAllExecutionListener.injectableAnnotations] contains the [Autowired] and a class
     * initialised with a property that contains this parameter will be correctly initialised and the mock property
     * is stored and accessible in the map.
     */
    @Test
    fun testInjectableAnnotations_containsAutowired()
    {
        testAnnotationExistsAndIsCreatedForClass(Autowired::class.java, BaseClass())
    }

    /**
     * Ensure [MockAllExecutionListener.injectableAnnotations] contains the [Resource] and a class
     * initialised with a property that contains this parameter will be correctly initialised and the mock property
     * is stored and accessible in the map.
     */
    @Test
    fun testInjectableAnnotations_containsJavaxResource()
    {
        testAnnotationExistsAndIsCreatedForClass(Resource::class.java, ChildClass())
    }

    /**
     * Ensure [MockAllExecutionListener.injectableAnnotations] contains the [jakarta.annotation.Resource] and a class
     * initialised with a property that contains this parameter will be correctly initialised and the mock property
     * is stored and accessible in the map.
     */
    @Test
    fun testInjectableAnnotations_containsJakartaResource()
    {
        testAnnotationExistsAndIsCreatedForClass(jakarta.annotation.Resource::class.java, ChildClass())
    }

    /**
     * A helper method to check if the provided class has any fields of with the provided annotation. If it does, it
     * will run [MockAllExecutionListener.prepareTestInstance] on the [instance] to confirm the annotated property is loaded
     * in the map.
     *
     * @param annotationClass the annotation class that we are expecting at least one of the class properties to have
     * @param instance the instance of object that we will be running through [MockAllExecutionListener.prepareTestInstance]
     */
    private fun <A: Annotation, T: Any>testAnnotationExistsAndIsCreatedForClass(annotationClass: Class<A>, instance: T)
    {
        val listener = MockAllExecutionListener()
        Assertions.assertTrue(listener.injectableAnnotations().contains(annotationClass))

        listener.prepareTestInstance(initialiseContextWithInstance(instance))
        val fieldWithAnnotation = Arrays.stream(instance::class.java.declaredFields).filter { field ->
            ReflectionUtils.makeAccessible(field)
            field.getAnnotation(annotationClass) != null
        }.findFirst()
        Assertions.assertTrue(fieldWithAnnotation.isPresent)
        Assertions.assertNotNull(MockAllExecutionListener.getInstance(fieldWithAnnotation.get().type))
    }

    /**
     * A utility method to check if an incoming object is a mock, delegating to [Mockito.mockingDetails].
     *
     * @param obj the object to check if it is a mock
     * @return <code>true</code> if this object is a mock, otherwise false
     */
    private fun isMock(obj: Any): Boolean
    {
        return Mockito.mockingDetails(obj).isMock
    }

    /**
     * A utility method to check if an incoming object is a spy, delegating to [Mockito.mockingDetails].
     *
     * @param obj the object to check if it is a spy
     * @return <code>true</code> if this object is a spy, otherwise false
     */
    private fun isSpy(obj: Any): Boolean
    {
        return Mockito.mockingDetails(obj).isSpy
    }
}
