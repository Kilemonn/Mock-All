package au.kilemon.mockall.tests

import au.kilemon.mockall.models.BaseClass
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class used for scenarios in [au.kilemon.mockall.TestMockAllExecutionListener].
 *
 * @author github.com/Kilemonn
 */
class BaseTest
{
    @Autowired
    private lateinit var baseClass: BaseClass
}
