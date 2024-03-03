package au.kilemon.mockall.tests

import au.kilemon.mockall.models.ab.AbstractChildClass
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class used for scenarios in [au.kilemon.mockall.MockAllExecutionListenerTest].
 *
 * @author github.com/Kilemonn
 */
class AbstractClassTest
{
    @Autowired
    private lateinit var childClass: AbstractChildClass
}
