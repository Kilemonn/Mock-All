package au.kilemon.mockall.tests

import au.kilemon.mockall.models.ChildChildClass
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class used for scenarios in [au.kilemon.mockall.TestMockAllExecutionListener].
 *
 * @author github.com/Kilemonn
 */
class ChildChildTest
{
    @Autowired
    private lateinit var childChild: ChildChildClass
}
