package au.kilemon.mockall.models.ab

import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * An abstract base object used to test that the recursive class look up is correct.
 *
 * @author github.com/Kilemonn
 */
abstract class AbstractBaseClass
{
    @Autowired
    protected lateinit var properties: Properties
}
