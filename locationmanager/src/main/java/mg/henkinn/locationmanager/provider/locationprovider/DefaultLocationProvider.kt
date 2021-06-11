package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import mg.henkinn.locationmanager.helper.logging.LogUtils.logI
import mg.henkinn.locationmanager.listener.DialogListener


open class DefaultLocationProvider : LocationProvider(),
    ContinuousTaskRunner, LocationListener, DialogListener {
    private var defaultLocationSource: DefaultLocationSource? = null
    private var provider: String? = null
    private var gpsDialog: Dialog? = null
    override fun onDestroy() {
        super.onDestroy()
        gpsDialog = null
        sourceProvider.removeSwitchTask()
        sourceProvider.removeUpdateRequest()
        sourceProvider.removeLocationUpdates(this)
    }

    override fun cancel() {
        sourceProvider.getUpdateRequest()?.release()
        sourceProvider.getProviderSwitchTask()?.stop()
    }

    override fun onPause() {
        super.onPause()
        sourceProvider.getUpdateRequest()?.release()
        sourceProvider.getProviderSwitchTask()?.pause()
    }

    override fun onResume() {
        super.onResume()
        sourceProvider.getUpdateRequest()?.run()
        if (isWaiting) {
            sourceProvider.getProviderSwitchTask()?.resume()
        }
        if (isDialogShowing && isGPSProviderEnabled) {
            // User activated GPS by going settings manually
            gpsDialog?.dismiss()
            onGPSActivated()
        }
    }

    override val isDialogShowing: Boolean
        get() = gpsDialog?.isShowing ?: false

    override fun onActivityResult(requestCode: RequestCode, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.GPS_ENABLE) {
            if (isGPSProviderEnabled) {
                onGPSActivated()
            } else {
                logI("User didn't activate GPS, so continue with Network Provider")
                getLocationByNetwork()
            }
        }
    }

    override fun get() {
        isWaiting = true

        // First check for GPS
        if (isGPSProviderEnabled) {
            logI("GPS is already enabled, getting location...")
            askForLocation(LocationManager.GPS_PROVIDER)
        } else {
            // GPS is not enabled,
            if (getConfiguration()!!.defaultProviderConfiguration()!!
                    .askForEnableGPS() && activity != null
            ) {
                logI("GPS is not enabled, asking user to enable it...")
                askForEnableGPS()
            } else {
                logI("GPS is not enabled, moving on with Network...")
                getLocationByNetwork()
            }
        }
    }

    open fun askForEnableGPS() {
        val gpsDialogProvider = getConfiguration()!!.defaultProviderConfiguration()!!
            .gpsDialogProvider()
        gpsDialogProvider!!.dialogListener = this
        gpsDialog = gpsDialogProvider.getDialog(activity!!)
        gpsDialog?.show()
    }

    fun onGPSActivated() {
        logI("User activated GPS, listen for location")
        askForLocation(LocationManager.GPS_PROVIDER)
    }

    fun getLocationByNetwork() {
        if (isNetworkProviderEnabled) {
            logI("Network is enabled, getting location...")
            askForLocation(LocationManager.NETWORK_PROVIDER)
        } else {
            logI("Network is not enabled, calling fail...")
            onLocationFailed(FailType.NETWORK_NOT_AVAILABLE)
        }
    }

    open fun askForLocation(provider: String?) {
        sourceProvider.getProviderSwitchTask()?.stop()
        setCurrentProvider(provider)
        val locationIsAlreadyAvailable = checkForLastKnowLocation()
        if (getConfiguration()!!.keepTracking() || !locationIsAlreadyAvailable) {
            logI("Ask for location update...")
            notifyProcessChange()
            if (!locationIsAlreadyAvailable) {
                sourceProvider.getProviderSwitchTask()?.delayed(waitPeriod)
            }
            requestUpdateLocation()
        } else {
            logI("We got location, no need to ask for location updates.")
        }
    }

    open fun checkForLastKnowLocation(): Boolean {
        val lastKnownLocation: Location? = sourceProvider.getLastKnownLocation(provider)
        if (sourceProvider.isLocationSufficient(
                lastKnownLocation,
                getConfiguration()!!.defaultProviderConfiguration()!!.acceptableTimePeriod(),
                getConfiguration()!!.defaultProviderConfiguration()!!.acceptableAccuracy()
            )
        ) {
            logI("LastKnowLocation is usable.")
            onLocationReceived(lastKnownLocation!!)
            return true
        } else {
            logI("LastKnowLocation is not usable.")
        }
        return false
    }

    fun setCurrentProvider(provider: String?) {
        this.provider = provider
    }

    open fun notifyProcessChange() {
        listener?.onProcessTypeChanged(if (LocationManager.GPS_PROVIDER == provider) ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER else ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER)
    }

    open fun requestUpdateLocation() {
        val timeInterval =
            getConfiguration()!!.defaultProviderConfiguration()!!.requiredTimeInterval()
        val distanceInterval =
            getConfiguration()!!.defaultProviderConfiguration()!!.requiredDistanceInterval()
        sourceProvider.getUpdateRequest()?.run(provider, timeInterval, distanceInterval.toFloat())
    }

    val waitPeriod: Long
        get() = if (LocationManager.GPS_PROVIDER == provider) getConfiguration()!!.defaultProviderConfiguration()!!
            .gpsWaitPeriod() else getConfiguration()!!.defaultProviderConfiguration()!!
            .networkWaitPeriod()
    private val isNetworkProviderEnabled: Boolean
        get() = sourceProvider.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    private val isGPSProviderEnabled: Boolean
        get() = sourceProvider.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun onLocationReceived(location: Location) {
        listener?.onLocationChanged(location)
        isWaiting = false
    }

    fun onLocationFailed(type: FailType) {
        listener?.onLocationFailed(type)
        isWaiting = false
    }

    override fun onLocationChanged(location: Location) {
        if (sourceProvider.updateRequestIsRemoved()) {
            return
        }
        onLocationReceived(location)

        // Remove cancelLocationTask because we have already find location,
        // no need to switch or call fail
        if (!sourceProvider.switchTaskIsRemoved()) {
            sourceProvider.getProviderSwitchTask()?.stop()
        }
        if (!getConfiguration()!!.keepTracking()) {
            sourceProvider.getUpdateRequest()?.release()
            sourceProvider.removeLocationUpdates(this)
        }
    }

    /**
     * This callback will never be invoked on Android Q and above, and providers can be considered as always in the LocationProvider#AVAILABLE state.
     *
     * @see [](https://developer.android.com/reference/android/location/LocationListener.onStatusChanged
    ) */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        listener?.onStatusChanged(provider, status, extras)
    }

    override fun onProviderEnabled(provider: String?) {
        listener?.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String?) {
        listener?.onProviderDisabled(provider)
    }

    override fun runScheduledTask(taskId: String) {
        if (taskId == DefaultLocationSource.PROVIDER_SWITCH_TASK) {
            sourceProvider.getUpdateRequest()?.release()
            if (LocationManager.GPS_PROVIDER == provider) {
                logI("We waited enough for GPS, switching to Network provider...")
                getLocationByNetwork()
            } else {
                logI("Network Provider is not provide location in required period, calling fail...")
                onLocationFailed(FailType.TIMEOUT)
            }
        }
    }

    override fun onPositiveButtonClick() {
        val activityStarted = startActivityForResult(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            RequestCode.GPS_ENABLE.code
        )
        if (!activityStarted) {
            onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE)
        }
    }

    override fun onNegativeButtonClick() {
        logI("User didn't want to enable GPS, so continue with Network Provider")
        getLocationByNetwork()
    }

    // For test purposes
    internal fun setDefaultLocationSource(defaultLocationSource: DefaultLocationSource?) {
        this.defaultLocationSource = defaultLocationSource
    }

    private val sourceProvider: DefaultLocationSource
        get() {
            if (defaultLocationSource == null) {
                defaultLocationSource = DefaultLocationSource(context!!, this, this)
            }
            return defaultLocationSource!!
        }
}