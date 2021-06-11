package mg.henkinn.locationmanager.provider.locationprovider

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import mg.henkinn.locationmanager.constants.RequestCode


internal open class GooglePlayServicesLocationSource(
    context: Context,
    private val locationRequest: LocationRequest,
    private val sourceListener: SourceListener?
) :
    LocationCallback() {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    internal interface SourceListener : OnSuccessListener<LocationSettingsResponse?>,
        OnFailureListener {
        override fun onSuccess(locationSettingsResponse: LocationSettingsResponse?)
        override fun onFailure(exception: Exception)
        fun onLocationResult(locationResult: LocationResult?)
        fun onLastKnowLocationTaskReceived(task: Task<Location>)
    }

    open fun checkLocationSettings() {
        LocationServices.getSettingsClient(fusedLocationProviderClient.applicationContext)
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .build()
            )
            .addOnSuccessListener { locationSettingsResponse ->
                sourceListener?.onSuccess(
                    locationSettingsResponse
                )
            }
            .addOnFailureListener { exception -> sourceListener?.onFailure(exception) }
    }

    @Throws(SendIntentException::class)
    open fun startSettingsApiResolutionForResult(
        resolvable: ResolvableApiException,
        activity: Activity
    ) {
        resolvable.startResolutionForResult(activity, RequestCode.SETTINGS_API.code)
    }

    @SuppressLint("MissingPermission")
    open fun requestLocationUpdate() {
        // This method is suited for the foreground use cases
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            this,
            Looper.myLooper()!!
        )
    }

    open fun removeLocationUpdates(): Task<Void> {
        return fusedLocationProviderClient.removeLocationUpdates(this)
    }

    @SuppressLint("MissingPermission")
    open fun requestLastLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener { task ->
                sourceListener?.onLastKnowLocationTaskReceived(
                    task
                )
            }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        sourceListener?.onLocationResult(locationResult)
    }
}