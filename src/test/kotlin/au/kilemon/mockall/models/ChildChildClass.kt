package au.kilemon.mockall.models

import au.kilemon.mockall.NotMocked
import java.util.*

/**
 * A bottom level child class, used to test that the recursive class look up is correct.
 * This is used to test the [NotMocked] annotation.
 *
 * @author github.com/Kilemonn
 */
class ChildChildClass: ChildClass()
{
    @NotMocked([Properties::class])
    private lateinit var thread: Thread
}
