package au.kilemon.mockall.tests

import au.kilemon.mockall.MockAllExecutionListener
import au.kilemon.mockall.NotMocked
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.Properties

/**
 * A test class demonstrating [au.kilemon.mockall.MockAllExecutionListener] is able to look up and load
 * existing beans for member variables marked as [au.kilemon.mockall.NotMocked].
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestExecutionListeners(MockAllExecutionListener::class)
@ContextConfiguration(classes = [WithContextTest.WithContextTestConfiguration::class])
class WithContextTest
{
    companion object
    {
        const val PROPERTY_NAME = "Property_name"
        const val PROPERTY_VALUE = "myV4Lu3"
    }

    @Autowired
    @NotMocked
    private lateinit var map: HashMap<String, String>

    @Qualifier("myProperties")
    @Autowired
    @NotMocked
    private lateinit var properties: Properties

    @Autowired
    private lateinit var packages: Package

    /**
     * A Spring configuration that is used for this test class.
     *
     * @author github.com/Kilemonn
     */
    @TestConfiguration
    internal open class WithContextTestConfiguration
    {
        // The bean lookup by type automatically handles the "Primary" annotation.
        @Bean
        @Primary
        open fun getMap(): HashMap<String, String>
        {
            val map = HashMap<String, String>()
            map[PROPERTY_NAME] = PROPERTY_VALUE
            return map
        }

        @Bean
        open fun anotherMap(): HashMap<String, String>
        {
            return HashMap<String, String>()
        }

        @Bean
        open fun myProperties(): Properties
        {
            val props = Properties()
            props.setProperty(PROPERTY_NAME, PROPERTY_VALUE)
            return props
        }
    }

    /**
     * Ensure that creating this test configuration will check and wire in existing beans created in the [TestConfiguration]
     * before creating zero arg constructor objects.
     */
    @Test
    fun testBeanCreationAndWiring()
    {
        Assertions.assertNotNull(map)
        Assertions.assertFalse(Mockito.mockingDetails(map).isMock)
        var value = map[PROPERTY_NAME]
        Assertions.assertEquals(PROPERTY_VALUE, value)

        Assertions.assertNotNull(properties)
        Assertions.assertFalse(Mockito.mockingDetails(properties).isMock)
        value = properties.getProperty(PROPERTY_NAME)
        Assertions.assertEquals(PROPERTY_VALUE, value)

        Assertions.assertNotNull(packages)
        Assertions.assertTrue(Mockito.mockingDetails(packages).isMock)
    }
}
