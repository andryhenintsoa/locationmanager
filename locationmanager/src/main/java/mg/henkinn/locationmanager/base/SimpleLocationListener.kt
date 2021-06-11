package mg.henkinn.locationmanager.base

import android.os.Bundle

import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.listener.LocationListener


/**
 * Empty Location Listener in case you need only some of the methods from [LocationListener]
 * Only [LocationListener.onLocationChanged] and [LocationListener.onLocationFailed]
 * need to be overridden.
 */
abstract class SimpleLocationListener : LocationListener {
    override fun onProcessTypeChanged(processType: ProcessType) {}
    override fun onPermissionGranted(alreadyHadPermission: Boolean) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}
}