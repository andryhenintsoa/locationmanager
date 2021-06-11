package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnCancelListener
import com.google.android.gms.common.GoogleApiAvailability
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import mg.henkinn.locationmanager.listener.FallbackListener


internal open class DispatcherLocationSource(continuousTaskRunner: ContinuousTaskRunner?) {
    private val gpServicesSwitchTask: ContinuousTask = ContinuousTask(
        GOOGLE_PLAY_SERVICE_SWITCH_TASK,
        continuousTaskRunner!!
    )

    open fun createDefaultLocationProvider(): DefaultLocationProvider {
        return DefaultLocationProvider()
    }

    open fun createGooglePlayServicesLocationProvider(fallbackListener: FallbackListener?): GooglePlayServicesLocationProvider {
        return GooglePlayServicesLocationProvider(fallbackListener)
    }

    open fun gpServicesSwitchTask(): ContinuousTask {
        return gpServicesSwitchTask
    }

    open fun isGoogleApiAvailable(context: Context?): Int {
        return if (context == null) -1 else GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
    }

    open fun isGoogleApiErrorUserResolvable(gpServicesAvailability: Int): Boolean {
        return GoogleApiAvailability.getInstance().isUserResolvableError(gpServicesAvailability)
    }

    open fun getGoogleApiErrorDialog(
        activity: Activity?, gpServicesAvailability: Int, requestCode: RequestCode,
        onCancelListener: OnCancelListener?
    ): Dialog? {
        return if (activity == null) null else GoogleApiAvailability.getInstance()
            .getErrorDialog(activity, gpServicesAvailability, requestCode.code, onCancelListener)
    }

    companion object {
        const val GOOGLE_PLAY_SERVICE_SWITCH_TASK = "googlePlayServiceSwitchTask"
    }

}