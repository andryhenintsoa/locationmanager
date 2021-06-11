package mg.henkinn.locationmanager.provider.locationprovider

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import mg.henkinn.locationmanager.helper.UpdateRequest
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import java.util.*


public open class DefaultLocationSource(
    context: Context,
    continuousTaskRunner: ContinuousTaskRunner?,
    locationListener: LocationListener?
) {
    @SuppressLint("ServiceCast")
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var updateRequest: UpdateRequest?
    var cancelTask: ContinuousTask?
        private set

    init {
        updateRequest = UpdateRequest(locationManager, locationListener)
        cancelTask = ContinuousTask(
            PROVIDER_SWITCH_TASK,
            continuousTaskRunner!!
        )
    }


    open fun isProviderEnabled(provider: String): Boolean {
        return locationManager.isProviderEnabled(provider)
    }

    @SuppressLint("MissingPermission")
    open fun getLastKnownLocation(provider: String?): Location? {
        if(provider == null) return null
        return locationManager.getLastKnownLocation(provider)
    }

    open fun removeLocationUpdates(locationListener: LocationListener) {
        locationManager.removeUpdates(locationListener)
    }

    open fun removeUpdateRequest() {
        updateRequest!!.release()
        updateRequest = null
    }

    open fun removeSwitchTask() {
        cancelTask!!.stop()
        cancelTask = null
    }

    open fun switchTaskIsRemoved(): Boolean {
        return cancelTask == null
    }

    open fun updateRequestIsRemoved(): Boolean {
        return updateRequest == null
    }

    open fun getProviderSwitchTask(): ContinuousTask? {
        return cancelTask
    }

    open fun getUpdateRequest(): UpdateRequest? {
        return updateRequest
    }
    open fun isLocationSufficient(
        location: Location?,
        acceptableTimePeriod: Long,
        acceptableAccuracy: Float
    ): Boolean {
        if (location == null) return false
        val givenAccuracy: Float = location.accuracy
        val givenTime: Long = location.time
        val minAcceptableTime: Long = Date().time - acceptableTimePeriod
        return minAcceptableTime <= givenTime && acceptableAccuracy >= givenAccuracy
    }

    companion object {
        const val PROVIDER_SWITCH_TASK = "providerSwitchTask"
    }
}