package mg.henkinn.locationmanager.provider.permissionprovider

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.logging.LogUtils
import mg.henkinn.locationmanager.listener.PermissionListener
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider
import mg.henkinn.locationmanager.view.ContextProcessor
import java.lang.ref.WeakReference


abstract class PermissionProvider(
    requiredPermissions: Array<String>,
    rationaleDialogProvider: DialogProvider?
) {
    private var weakContextProcessor: WeakReference<ContextProcessor>? = null
    private var weakPermissionListener: WeakReference<PermissionListener>? = null
    internal val requiredPermissions: Array<String>
    internal val dialogProvider: DialogProvider?


    /**
     * This class is responsible to get required permissions, and notify [LocationManager].
     *
     * @param requiredPermissions are required, setting this field empty will {@throws IllegalStateException}
     * @param rationaleDialogProvider will be used to display rationale dialog when it is necessary. If this field is set
     * to null, then rationale dialog will not be displayed to user at all.
     */
    init {

        if (requiredPermissions.isEmpty()) {
            throw IllegalStateException("You cannot create PermissionProvider without any permission required.");
        }

        this.requiredPermissions = requiredPermissions;
        this.dialogProvider = rationaleDialogProvider;
    }

    /**
     * Return true if it is possible to ask permission, false otherwise
     */
    abstract fun requestPermissions(): Boolean

    /**
     * This method needs to be called when permission results are received
     */
    abstract fun onRequestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<String>, grantResults: IntArray
    )

    /**
     * This will be set internally by [LocationManager] before any call is executed on PermissionProvider
     */
    open var permissionListener: PermissionListener?
        get() = weakPermissionListener?.get()
        set(permissionListener) {
            weakPermissionListener = WeakReference(permissionListener)
        }

    protected val context: Context?
        get() = weakContextProcessor?.get()?.context

    protected val activity: Activity?
        get() = weakContextProcessor?.get()?.activity

    protected val fragment: Fragment?
        get() = weakContextProcessor?.get()?.fragment

    /**
     * This will be set internally by [LocationManager] before any call is executed on PermissionProvider
     */
    open fun setContextProcessor(contextProcessor: ContextProcessor?) {
        weakContextProcessor = WeakReference(contextProcessor)
    }

    /**
     * Return true if required permissions are granted, false otherwise
     */
    open fun hasPermission(): Boolean {
        if (context == null) {
            LogUtils.logE(
                "Couldn't check whether permissions are granted or not "
                        + "because of PermissionProvider doesn't contain any context."
            )
            return false
        }
        for (permission in requiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // For test purposes
    protected open fun checkSelfPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(context!!, permission)
    }
}