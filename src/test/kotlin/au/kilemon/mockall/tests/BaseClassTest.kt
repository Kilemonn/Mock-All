package au.kilemon.mockall.tests

import au.kilemon.mockall.models.BaseClass
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class used for scenarios in [au.kilemon.mockall.MockAllExecutionListenerTest].
 *
 * @author github.com/Kilemonn
 */
class BaseClassTest
{
    @Autowired
    private lateinit var baseClass: BaseClass
}
