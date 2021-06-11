package mg.henkinn.locationmanager.provider.permissionprovider

import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import mg.henkinn.locationmanager.constants.RequestCode


open class PermissionCompatSource {
    open fun shouldShowRequestPermissionRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    open fun requestPermissions(
        fragment: Fragment,
        requiredPermissions: Array<String>,
        requestCode: RequestCode
    ) {
        fragment.requestPermissions(requiredPermissions, requestCode.code)
    }

    open fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    open fun requestPermissions(
        activity: Activity,
        requiredPermissions: Array<String>,
        requestCode: RequestCode
    ) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, requestCode.code)
    }
}