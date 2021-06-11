package mg.henkinn.locationmanager.helper

import android.annotation.SuppressLint
import android.location.LocationListener;
import android.location.LocationManager;


open class UpdateRequest(private val locationManager: LocationManager?,
                    private val locationListener: LocationListener?
) {
    private var provider: String? = null
    private var minTime: Long = 0
    private var minDistance = 0f
    open fun run(provider: String?, minTime: Long, minDistance: Float) {
        this.provider = provider
        this.minTime = minTime
        this.minDistance = minDistance
        run()
    }

    @SuppressLint("MissingPermission")
    open fun run() {
        if (provider?.isNotEmpty() == true) {
            locationManager?.requestLocationUpdates(provider!!,minTime,minDistance,locationListener)
        }
    }

    open fun release() {
        locationManager?.removeUpdates(locationListener)
    }

}