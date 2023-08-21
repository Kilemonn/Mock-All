package au.kilemon.mockall.models

import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * A base object used to test that the recursive class look up is correct.
 * This is also the class used to test the [Autowired] annotation.
 *
 * @author github.com/Kilemonn
 */
open class BaseClass
{
    @Autowired
    private lateinit var properties: Properties
}
