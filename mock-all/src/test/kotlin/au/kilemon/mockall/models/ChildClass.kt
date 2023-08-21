package au.kilemon.mockall.models

import javax.annotation.Resource

/**
 * A middle level child object, used to test that the recursive class look up is correct.
 * This uses the [jakarta.annotation.Resource] and [Resource] annotations.
 *
 * @author github.com/Kilemonn
 */
open class ChildClass: BaseClass()
{
    @jakarta.annotation.Resource
    private lateinit var moduleResource: Module

    @Resource
    private lateinit var packageResource: Package
}
