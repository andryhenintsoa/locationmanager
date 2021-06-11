package mg.henkinn.locationmanager.fakes

import android.content.pm.PackageManager
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider
import mg.henkinn.locationmanager.provider.permissionprovider.PermissionProvider


class FakePermissionProvider(
    requiredPermissions: Array<String>,
    rationaleDialogProvider: DialogProvider?
) :
    PermissionProvider(requiredPermissions, rationaleDialogProvider) {
    private var requestPermissions = false
    private var isPermissionGranted = false
    fun shouldSuccessOnRequest(success: Boolean) {
        requestPermissions = success
    }

    fun grantPermission(granted: Boolean) {
        isPermissionGranted = granted
    }

    override fun checkSelfPermission(permission: String): Int {
        return if (isPermissionGranted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
    }

    override fun requestPermissions(): Boolean {
        return requestPermissions
    }

    override fun onRequestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
    }
}