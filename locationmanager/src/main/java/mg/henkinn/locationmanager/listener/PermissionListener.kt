package mg.henkinn.locationmanager.listener


interface PermissionListener {
    /**
     * Notify when user is granted all required permissions
     */
    fun onPermissionsGranted()

    /**
     * Notify when user is denied any one of required permissions
     */
    fun onPermissionsDenied()
}