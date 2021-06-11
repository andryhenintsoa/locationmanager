package mg.henkinn.locationmanager.view

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference


open class ContextProcessor(context: Context) {

    private val applicationContext: Context
    private var weakActivity: WeakReference<Activity>
    private var weakFragment: WeakReference<Fragment>

    init {
        require(context is Application) { "ContextProcessor can only be initialized with Application!" }
        applicationContext = context
        weakActivity = WeakReference(null)
        weakFragment = WeakReference(null)
    }

    /**
     * In order to use in Activity or Service
     */
    fun setActivity(activity: Activity): ContextProcessor {
        weakActivity = WeakReference(activity)
        weakFragment = WeakReference(null)
        return this
    }

    /**
     * In order to use in Fragment
     */
    fun setFragment(fragment: Fragment): ContextProcessor {
        weakActivity = WeakReference(null)
        weakFragment = WeakReference(fragment)
        return this
    }

    open val fragment: Fragment?
        get() = weakFragment.get()

    open val activity: Activity?
        get() {
            return weakActivity.get() ?: weakFragment.get()?.activity
        }
    open val context: Context
        get() = applicationContext
}