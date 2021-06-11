package mg.henkinn.locationmanager.helper.continuoustask

import android.os.Handler


open class ContinuousTask(
    private val taskId: String,
    continuousTaskRunner: ContinuousTaskRunner
) :
    Handler(), Runnable {
    private val continuousTaskScheduler: ContinuousTaskScheduler
    private val continuousTaskRunner: ContinuousTaskRunner

    interface ContinuousTaskRunner {
        /**
         * Callback to take action when scheduled time is arrived.
         * Called with given taskId in order to distinguish which task should be run,
         * in case of same [ContinuousTaskRunner] passed to multiple Tasks
         */
        fun runScheduledTask(taskId: String)
    }

    init {
        continuousTaskScheduler = ContinuousTaskScheduler(this)
        this.continuousTaskRunner = continuousTaskRunner
    }

    open fun delayed(delay: Long) {
        continuousTaskScheduler.delayed(delay)
    }

    open fun pause() {
        continuousTaskScheduler.onPause()
    }

    open fun resume() {
        continuousTaskScheduler.onResume()
    }

    open fun stop() {
        continuousTaskScheduler.onStop()
    }

    override fun run() {
        continuousTaskRunner.runScheduledTask(taskId)
    }

    fun schedule(delay: Long) {
        postDelayed(this, delay)
    }

    fun unregister() {
        removeCallbacks(this)
    }

    open val currentTime: Long
        get() = System.currentTimeMillis()
}