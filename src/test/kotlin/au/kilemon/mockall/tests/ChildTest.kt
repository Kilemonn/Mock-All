package au.kilemon.mockall.tests

import au.kilemon.mockall.models.ChildClass
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class used for scenarios in [au.kilemon.mockall.TestMockAllExecutionListener].
 *
 * @author github.com/Kilemonn
 */
class ChildTest
{
    @Autowired
    private lateinit var childClass: ChildClass
}
