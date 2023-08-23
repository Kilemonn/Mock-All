package au.kilemon.mockall.models

import au.kilemon.mockall.NotMocked
import java.util.*

/**
 * A model class used to ensure that when we put the [NotMocked] annotation on a property
 * and include it in the [NotMocked.spyClasses] list that the property is actually constructed as a spy.
 *
 * @author github.com/Kilemonn
 */
class NotMockedSpy
{
    @NotMocked([Properties::class])
    public lateinit var properties: Properties
}
