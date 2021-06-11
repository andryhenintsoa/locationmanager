package mg.henkinn.locationmanager.base

import android.app.Service
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import mg.henkinn.locationmanager.LocationManager
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.listener.LocationListener


abstract class LocationBaseService() : Service(), LocationListener {
    private var locationManager: LocationManager? = null
    abstract val locationConfiguration: LocationConfiguration

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = LocationManager.Builder(applicationContext)
            .configuration(locationConfiguration)
            .notify(this)
            .build()
        return super.onStartCommand(intent, flags, startId)
    }

    protected fun getLocationManager(): LocationManager? {
        return locationManager
    }

    protected fun getLocation() {
        if (locationManager != null) {
            locationManager?.get()
        } else {
            throw IllegalStateException(
                "locationManager is null. "
                        + "Make sure you call super.onStartCommand before attempting to getLocation"
            )
        }
    }

    override fun onProcessTypeChanged(processType: ProcessType) {
        // override if needed
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        // override if needed
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // override if needed
    }

    override fun onProviderEnabled(provider: String?) {
        // override if needed
    }

    override fun onProviderDisabled(provider: String?) {
        // override if needed
    }
}