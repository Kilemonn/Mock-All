package au.kilemon.mockall.models.ab

import org.springframework.beans.factory.annotation.Autowired
import javax.annotation.Resource

/**
 * A base object used to test that the recursive class look up is correct.
 * This is also the class used to test the [Autowired] annotation.
 *
 * @author github.com/Kilemonn
 */
class AbstractChildClass : AbstractBaseClass()
{
    @Resource
    private lateinit var packageResource: Package
}
