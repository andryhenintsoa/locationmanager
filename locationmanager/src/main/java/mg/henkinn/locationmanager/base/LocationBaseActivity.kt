package mg.henkinn.locationmanager.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import mg.henkinn.locationmanager.LocationManager
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.listener.LocationListener


abstract class LocationBaseActivity() : AppCompatActivity(), LocationListener {
    private var locationManager: LocationManager? = null
    abstract val locationConfiguration: LocationConfiguration
    private var foo = false

    protected fun getLocationManager(): LocationManager? {
        return locationManager
    }

    fun getLocation() {
        if (locationManager != null) {

//            if (foo) {
//                locationManager!!.cancel()
//            } else {
//                locationManager!!.get()
//            }
//            foo = !foo

            locationManager!!.get()
        } else {
            throw IllegalStateException(
                "locationManager is null. "
                        + "Make sure you call super.initialize before attempting to getLocation"
            )
        }
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager.Builder(applicationContext)
            .configuration(locationConfiguration)
            .activity(this)
            .notify(this)
            .build()
    }

    @CallSuper
    override fun onDestroy() {
        locationManager?.onDestroy()
        super.onDestroy()
    }

    @CallSuper
    override fun onPause() {
        locationManager?.onPause()
        super.onPause()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        locationManager?.onResume()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationManager?.onActivityResult(
            RequestCode.values().first { it.code == requestCode },
            resultCode,
            data
        )
    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager?.onRequestPermissionsResult(
            RequestCode.values().first { it.code == requestCode }, permissions, grantResults
        )
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