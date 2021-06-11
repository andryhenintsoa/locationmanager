package mg.henkinn.locationmanager.helper.continuoustask

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ContinuousTaskSchedulerTest {

    companion object {
        private const val INITIAL_TIME = 10000L
        private const val DELAY = 1000L
        private const val DURATION = 50L
    }

    @Mock
    lateinit var continuousTask: ContinuousTask
    private var continuousTaskScheduler: ContinuousTaskScheduler? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        continuousTaskScheduler = ContinuousTaskScheduler(continuousTask)
        `when`(continuousTask.currentTime).thenReturn(INITIAL_TIME)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedNotCalledIsSetShouldReturnFalse() {
        assertFalse(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedCalledIsSetShouldReturnTrue() {
        continuousTaskScheduler!!.delayed(0)
        assertTrue(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnPauseCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onPause()
        assertFalse(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnResumeCalledIsSetShouldReturnTrue() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onResume()
        assertTrue(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnStopCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onStop()
        assertFalse(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenCleanCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.clean()
        assertFalse(continuousTaskScheduler!!.isSet)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedNotCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.onPause()
        verify(continuousTask, never()).unregister()
        continuousTaskScheduler!!.set(0)
        verify(continuousTask, never()).delayed(0)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedCalledTaskShouldSchedule() {
        continuousTaskScheduler!!.delayed(DELAY)
        verify(continuousTask).schedule(DELAY)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnPauseCalledTaskShouldUnregister() {
        continuousTaskScheduler!!.delayed(DELAY)
        continuousTaskScheduler!!.onPause()
        verify(continuousTask).unregister()
    }

    @Test
    @Throws(Exception::class)
    fun whenOnResumeCalledTaskShouldReScheduled() {
        continuousTaskScheduler!!.delayed(DELAY)
        verify(continuousTask).schedule(DELAY)
        `when`(continuousTask.currentTime).thenReturn(INITIAL_TIME + DURATION)
        continuousTaskScheduler!!.onPause()
        verify(continuousTask).unregister()
        continuousTaskScheduler!!.onResume()
        verify(continuousTask).schedule(DELAY - DURATION)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnStopCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.delayed(0)
        verify(continuousTask).currentTime
        verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.onStop()
        verify(continuousTask).unregister()
        continuousTaskScheduler!!.onPause()
        continuousTaskScheduler!!.onResume()
        verifyNoMoreInteractions(continuousTask)
    }

    @Test
    @Throws(Exception::class)
    fun whenCleanCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.delayed(0)
        verify(continuousTask).currentTime
        verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.clean()
        continuousTaskScheduler!!.onPause()
        continuousTaskScheduler!!.onResume()
        verifyNoMoreInteractions(continuousTask)
    }

    @Test
    @Throws(Exception::class)
    fun whenTaskIsAlreadyScheduledOnResumeShouldHaveNoInteraction() {
        continuousTaskScheduler!!.delayed(0)
        verify(continuousTask).currentTime
        verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.onResume()
        verifyNoMoreInteractions(continuousTask)
    }
}