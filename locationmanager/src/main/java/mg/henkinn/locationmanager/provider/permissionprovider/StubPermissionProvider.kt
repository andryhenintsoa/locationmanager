package mg.henkinn.locationmanager.provider.permissionprovider

import mg.henkinn.locationmanager.configuration.Defaults
import mg.henkinn.locationmanager.constants.RequestCode


class StubPermissionProvider :
    PermissionProvider(Defaults.LOCATION_PERMISSIONS, null) {
    override fun requestPermissions(): Boolean {
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        TODO("Not yet implemented")
    }
}