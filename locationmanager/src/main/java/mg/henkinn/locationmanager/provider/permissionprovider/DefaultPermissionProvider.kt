package mg.henkinn.locationmanager.provider.permissionprovider

import android.content.pm.PackageManager
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.logging.LogUtils
import mg.henkinn.locationmanager.listener.DialogListener
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider


open class DefaultPermissionProvider(
    requiredPermissions: Array<String>,
    dialogProvider: DialogProvider?
) :
    PermissionProvider(requiredPermissions, dialogProvider), DialogListener {
    // For test purposes
    internal var permissionCompatSource: PermissionCompatSource? = null
        get() {
            if (field == null) {
                field = PermissionCompatSource()
            }
            return field
        }
        set

    override fun requestPermissions(): Boolean {
        if (activity == null) {
            LogUtils.logI(
                "Cannot ask for permissions, "
                        + "because DefaultPermissionProvider doesn't contain an Activity instance."
            )
            return false
        }
        if (shouldShowRequestPermissionRationale()) {
            dialogProvider?.dialogListener = this
            dialogProvider?.getDialog(activity!!)?.show()
        } else {
            executePermissionsRequest()
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == RequestCode.RUNTIME_PERMISSION) {

            // Check if any of required permissions are denied.
            var isDenied = false
            var i = 0
            val size = permissions.size
            while (i < size) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isDenied = true
                }
                i++
            }
            if (isDenied) {
                LogUtils.logI("User denied some of required permissions, task will be aborted!")
                permissionListener?.onPermissionsDenied()
            } else {
                LogUtils.logI("We got all required permission!")
                permissionListener?.onPermissionsGranted()
            }
        }
    }

    override fun onPositiveButtonClick() {
        executePermissionsRequest()
    }

    override fun onNegativeButtonClick() {
        LogUtils.logI("User didn't even let us to ask for permission!")
        permissionListener?.onPermissionsDenied()
    }

    fun shouldShowRequestPermissionRationale(): Boolean {
        var shouldShowRationale = false
        for (permission: String in requiredPermissions) {
            shouldShowRationale = shouldShowRationale || checkRationaleForPermission(permission)
        }
        LogUtils.logI("Should show rationale dialog for required permissions: $shouldShowRationale")
        return shouldShowRationale && (activity != null) && (dialogProvider != null)
    }

    fun checkRationaleForPermission(permission: String): Boolean {
        return when {
            fragment != null -> {
                permissionCompatSource!!.shouldShowRequestPermissionRationale(
                    fragment!!,
                    permission
                )
            }
            activity != null -> {
                permissionCompatSource!!.shouldShowRequestPermissionRationale(activity!!, permission)
            }
            else -> {
                false
            }
        }
    }

    open fun executePermissionsRequest() {
        LogUtils.logI("Asking for Runtime Permissions...")
        when {
            fragment != null -> {
                permissionCompatSource!!.requestPermissions(
                    fragment!!,
                    requiredPermissions, RequestCode.RUNTIME_PERMISSION
                )
            }
            activity != null -> {
                permissionCompatSource!!.requestPermissions(
                    activity!!,
                    requiredPermissions, RequestCode.RUNTIME_PERMISSION
                )
            }
            else -> {
                LogUtils.logE("Something went wrong requesting for permissions.")
                if (permissionListener != null) permissionListener!!.onPermissionsDenied()
            }
        }
    }
}