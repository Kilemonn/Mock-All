package au.kilemon.mockall

import kotlin.reflect.KClass

/**
 * An annotation needed for [MockAllExecutionListener].
 * This is used to specify which is the main bean that is going to be tested, and also specify a list of [KClass]
 * that should be created as [org.mockito.Mockito.spy] objects instead of [org.mockito.Mockito.mock]s for this test.
 *
 * @author github.com/Kilemonn
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NotMocked(val spyClasses: Array<KClass<*>> = [])
