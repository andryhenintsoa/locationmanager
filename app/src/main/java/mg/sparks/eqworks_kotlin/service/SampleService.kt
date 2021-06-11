package mg.sparks.eqworks_kotlin.service

import android.content.Intent
import android.location.Location
import android.os.IBinder
import mg.henkinn.locationmanager.base.LocationBaseService
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.configuration.Configurations
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType


class SampleService : LocationBaseService() {
    private var isLocationRequested = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override val locationConfiguration: LocationConfiguration
        get() = Configurations.silentConfiguration(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // calling super is required when extending from LocationBaseService
        super.onStartCommand(intent, flags, startId)
        if (!isLocationRequested) {
            isLocationRequested = true
            getLocation()
        }

        // Return type is depends on your requirements
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
        val intent = Intent(ACTION_LOCATION_CHANGED)
        intent.putExtra(EXTRA_LOCATION, location)
        sendBroadcast(intent)
        stopSelf()
    }

    override fun onLocationFailed(type: FailType) {
        val intent = Intent(ACTION_LOCATION_FAILED)
        intent.putExtra(EXTRA_FAIL_TYPE, type.code)
        sendBroadcast(intent)
        stopSelf()
    }

    override fun onProcessTypeChanged(processType: ProcessType) {
        val intent = Intent(ACTION_PROCESS_CHANGED)
        intent.putExtra(EXTRA_PROCESS_TYPE, processType.code)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_LOCATION_CHANGED =
            "mg.henkinn.locationmanager.sample.service.LOCATION_CHANGED"
        const val ACTION_LOCATION_FAILED =
            "mg.henkinn.locationmanager.sample.service.LOCATION_FAILED"
        const val ACTION_PROCESS_CHANGED =
            "mg.henkinn.locationmanager.sample.service.PROCESS_CHANGED"
        const val EXTRA_LOCATION = "ExtraLocationField"
        const val EXTRA_FAIL_TYPE = "ExtraFailTypeField"
        const val EXTRA_PROCESS_TYPE = "ExtraProcessTypeField"
    }
}