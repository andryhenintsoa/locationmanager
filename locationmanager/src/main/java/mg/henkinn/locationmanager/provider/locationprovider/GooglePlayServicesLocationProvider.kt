package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.logging.LogUtils.logE
import mg.henkinn.locationmanager.helper.logging.LogUtils.logI
import mg.henkinn.locationmanager.listener.FallbackListener
import java.lang.ref.WeakReference


open class GooglePlayServicesLocationProvider internal constructor(fallbackListener: FallbackListener?) :
    LocationProvider(), GooglePlayServicesLocationSource.SourceListener {
    private val fallbackListener: WeakReference<FallbackListener> = WeakReference(fallbackListener)
    final override var isDialogShowing = false
        private set
    private var googlePlayServicesLocationSource: GooglePlayServicesLocationSource? = null

    override fun onResume() {
        if (!isDialogShowing && (isWaiting || getConfiguration()!!.keepTracking())) {
            requestLocationUpdate()
        }
    }

    override fun onPause() {
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (!isDialogShowing && googlePlayServicesLocationSource != null) {
            removeLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) removeLocationUpdates()
    }

    override fun get() {
        isWaiting = true
        if (context != null) {
            logI("Start request location updates.")
            if (getConfiguration()!!.googlePlayServicesConfiguration()!!.ignoreLastKnowLocation()) {
                logI("Configuration requires to ignore last know location from GooglePlayServices Api.")

                // Request fresh location
                locationRequired()
            } else {
                // Try to get last location, if failed then request fresh location
                sourceProvider.requestLastLocation()
            }
        } else {
            failed(FailType.VIEW_DETACHED)
        }
    }

    override fun cancel() {
        logI("Canceling GooglePlayServiceLocationProvider...")
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            removeLocationUpdates()
        }
    }

    override fun onActivityResult(requestCode: RequestCode, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.SETTINGS_API) {
            isDialogShowing = false
            if (resultCode == Activity.RESULT_OK) {
                logI("We got settings changed, requesting location update...")
                requestLocationUpdate()
            } else {
                logI("User denied settingsApi dialog, GooglePlayServices SettingsApi failing...")
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
            }
        }
    }

    open fun onLocationChanged(location: Location) {

        listener?.onLocationChanged(location)


        // Set waiting as false because we got at least one, even though we keep tracking user's location
        isWaiting = false
        if (!getConfiguration()!!.keepTracking()) {
            // If need to update location once, clear the listener to prevent multiple call
            logI("We got location and no need to keep tracking, so location update is removed.")
            removeLocationUpdates()
        }
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        if (locationResult == null) {
            // Do nothing, wait for other result
            return
        }
        for (location: Location in locationResult.locations) {
            onLocationChanged(location)
        }
    }

    override fun onSuccess(locationSettingsResponse: LocationSettingsResponse?) {
        // All location settings are satisfied. The client can initialize location
        // requests here.
        logI(
            "We got GPS, Wifi and/or Cell network providers enabled enough "
                    + "to receive location as we needed. Requesting location update..."
        )
        requestLocationUpdate()
    }

    override fun onFailure(exception: Exception) {
        when (val statusCode = (exception as ApiException).statusCode) {
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                // Location settings are not satisfied.
                // However, we have no way to fix the settings so we won't show the dialog.
                logE("Settings change is not available, SettingsApi failing...")
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                 // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                // Cast to a resolvable exception.
                resolveSettingsApi(exception as ResolvableApiException)
            else -> {
                // for other CommonStatusCodes values
                logE(
                    "LocationSettings failing, status: " + CommonStatusCodes.getStatusCodeString(
                        statusCode
                    )
                )
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
            }
        }
    }

    open fun resolveSettingsApi(resolvable: ResolvableApiException) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            logI("We need settingsApi dialog to switch required settings on.")
            if (activity != null) {
                logI("Displaying the dialog...")
                sourceProvider.startSettingsApiResolutionForResult(resolvable, activity!!)
                isDialogShowing = true
            } else {
                logI("Settings Api cannot show dialog if LocationManager is not running on an activity!")
                settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE)
            }
        } catch (e: SendIntentException) {
            logE("Error on displaying SettingsApi dialog, SettingsApi failing...")
            settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
        }
    }

    /**
     * Task result can be null in certain conditions
     * See: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#getLastLocation()
     */
    override fun onLastKnowLocationTaskReceived(task: Task<Location>) {
        if (task.isSuccessful && task.result != null) {
            val lastKnownLocation: Location = task.result
            logI("LastKnowLocation is available.")
            onLocationChanged(lastKnownLocation)
            if (getConfiguration()!!.keepTracking()) {
                logI("Configuration requires keepTracking.")
                locationRequired()
            }
        } else {
            logI("LastKnowLocation is not available.")
            locationRequired()
        }
    }

    fun locationRequired() {
        logI("Ask for location update...")
        if (getConfiguration()!!.googlePlayServicesConfiguration()!!.askForSettingsApi()) {
            logI("Asking for SettingsApi...")
            sourceProvider.checkLocationSettings()
        } else {
            logI("SettingsApi is not enabled, requesting for location update...")
            requestLocationUpdate()
        }
    }

    open fun requestLocationUpdate() {
        listener?.onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES)
        logI("Requesting location update...")
        sourceProvider.requestLocationUpdate()
    }

    fun settingsApiFail(failType: FailType) {
        if (getConfiguration()!!.googlePlayServicesConfiguration()!!.failOnSettingsApiSuspended()) {
            failed(failType)
        } else {
            logE(
                ("Even though settingsApi failed, configuration requires moving on. "
                        + "So requesting location update...")
            )
            requestLocationUpdate()
        }
    }

    fun failed(type: FailType) {
        if (getConfiguration()!!.googlePlayServicesConfiguration()!!
                .fallbackToDefault()
        ) {
            fallbackListener.get()?.onFallback()
        } else {
            listener?.onLocationFailed(type)
        }
        isWaiting = false
    }

    // For test purposes
    internal fun setDispatcherLocationSource(googlePlayServicesLocationSource: GooglePlayServicesLocationSource?) {
        this.googlePlayServicesLocationSource = googlePlayServicesLocationSource
    }

    private val sourceProvider: GooglePlayServicesLocationSource
        get() {
            if (googlePlayServicesLocationSource == null) {
                googlePlayServicesLocationSource = GooglePlayServicesLocationSource(
                    context!!,
                    getConfiguration()!!.googlePlayServicesConfiguration()!!.locationRequest(),
                    this
                )
            }
            return googlePlayServicesLocationSource!!
        }

    private fun removeLocationUpdates() {
        logI("Stop location updates...")

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            isWaiting = false
            googlePlayServicesLocationSource!!.removeLocationUpdates()
        }
    }
}