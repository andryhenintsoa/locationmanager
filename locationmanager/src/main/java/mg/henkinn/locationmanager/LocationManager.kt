package mg.henkinn.locationmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.logging.DefaultLogger
import mg.henkinn.locationmanager.helper.logging.LogUtils
import mg.henkinn.locationmanager.helper.logging.LogUtils.enable
import mg.henkinn.locationmanager.helper.logging.LogUtils.logI
import mg.henkinn.locationmanager.helper.logging.Logger
import mg.henkinn.locationmanager.listener.LocationListener
import mg.henkinn.locationmanager.listener.PermissionListener
import mg.henkinn.locationmanager.provider.locationprovider.DispatcherLocationProvider
import mg.henkinn.locationmanager.provider.locationprovider.LocationProvider
import mg.henkinn.locationmanager.provider.permissionprovider.PermissionProvider
import mg.henkinn.locationmanager.view.ContextProcessor


class LocationManager private constructor(builder: Builder) :
    PermissionListener {
    private val listener: LocationListener?

    /**
     * Returns configuration object which is defined to this manager
     */
    val configuration: LocationConfiguration?
    private val activeProvider: LocationProvider?
    private val permissionProvider: PermissionProvider?

    /**
     * To create an instance of this manager you MUST specify a LocationConfiguration
     */
    init {
        listener = builder.listener
        configuration = builder.configuration
        activeProvider = builder.activeProvider
        permissionProvider = configuration!!.permissionConfiguration().permissionProvider()
        permissionProvider!!.setContextProcessor(builder.contextProcessor)
        permissionProvider.permissionListener = this
    }

    class Builder(internal var contextProcessor: ContextProcessor) {
        internal var listener: LocationListener? = null
        internal var configuration: LocationConfiguration? = null
        internal var activeProvider: LocationProvider? = null

        /**
         * Builder object to create LocationManager
         *
         * @param context MUST be an application context
         */
        constructor(context: Context) : this(ContextProcessor(context)) {
        }

        /**
         * Activity is required in order to ask for permission, GPS enable dialog, Rationale dialog,
         * GoogleApiClient and SettingsApi.
         *
         * @param activity will be kept as weakReference
         */
        fun activity(activity: Activity): Builder {
            contextProcessor.setActivity(activity)
            return this
        }

        /**
         * Fragment is required in order to ask for permission, GPS enable dialog, Rationale dialog,
         * GoogleApiClient and SettingsApi.
         *
         * @param fragment will be kept as weakReference
         */
        fun fragment(fragment: Fragment): Builder {
            contextProcessor.setFragment(fragment)
            return this
        }

        /**
         * Configuration object in order to take decisions accordingly while trying to retrieve location
         */
        fun configuration(locationConfiguration: LocationConfiguration): Builder {
            configuration = locationConfiguration
            return this
        }

        /**
         * Instead of using [DispatcherLocationProvider] you can create your own,
         * and set it to manager so it will use given one.
         */
        fun locationProvider(provider: LocationProvider): Builder {
            activeProvider = provider
            return this
        }

        /**
         * Specify a LocationListener to receive location when it is available,
         * or get knowledge of any other steps in process
         */
        fun notify(listener: LocationListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): LocationManager {
            checkNotNull(configuration) { "You must set a configuration object." }
            if (activeProvider == null) {
                locationProvider(DispatcherLocationProvider())
            }
            activeProvider!!.configure(
                contextProcessor,
                configuration,
                listener
            )
            return LocationManager(this)
        }
    }

    /**
     * Google suggests to stop location updates when the activity is no longer in focus
     * http://developer.android.com/training/location/receive-location-updates.html#stop-updates
     */
    fun onPause() {
        activeProvider?.onPause()
    }

    /**
     * Restart location updates to keep continue getting locations when activity is back
     */
    fun onResume() {
        activeProvider?.onResume()
    }

    /**
     * Release whatever you need to when onDestroy is called
     */
    fun onDestroy() {
        activeProvider?.onDestroy()
    }

    /**
     * This is required to check when user handles with Google Play Services error, or enables GPS...
     */
    fun onActivityResult(requestCode: RequestCode, resultCode: Int, data: Intent?) {
        activeProvider?.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Provide requestPermissionResult to manager so the it can handle RuntimePermission
     */
    fun onRequestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionProvider!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * To determine whether LocationManager is currently waiting for location or it did already receive one!
     */
    val isWaitingForLocation: Boolean
        get() = activeProvider?.isWaiting ?: false

    /**
     * To determine whether the manager is currently displaying any dialog or not
     */
    val isAnyDialogShowing: Boolean
        get() = activeProvider?.isDialogShowing ?: false

    /**
     * Abort the mission and cancel all location update requests
     */
    fun cancel() {
        activeProvider?.cancel()
    }

    /**
     * The only method you need to call to trigger getting location process
     */
    fun get() {
        askForPermission()
    }

    /**
     * Only For Test Purposes
     */
    fun activeProvider(): LocationProvider? {
        return activeProvider
    }

    fun askForPermission() {
        if (permissionProvider!!.hasPermission()) {
            permissionGranted(true)
        } else {
            listener?.onProcessTypeChanged(ProcessType.ASKING_PERMISSIONS)
            if (permissionProvider.requestPermissions()) {
                logI("Waiting until we receive any callback from PermissionProvider...")
            } else {
                logI("Couldn't get permission, Abort!")
                failed(FailType.PERMISSION_DENIED)
            }
        }
    }

    private fun permissionGranted(alreadyHadPermission: Boolean) {
        logI("Permission got : $alreadyHadPermission !")
        listener?.onPermissionGranted(alreadyHadPermission)
        activeProvider?.get()
    }

    private fun failed(type: FailType) {
        listener?.onLocationFailed(type)
    }

    override fun onPermissionsGranted() {
        permissionGranted(false)
    }

    override fun onPermissionsDenied() {
        failed(FailType.PERMISSION_DENIED)
    }

    companion object {
        /**
         * Library tries to log as much as possible in order to make it transparent to see what is actually going on
         * under the hood. You can enable it for debug purposes, but do not forget to disable on production.
         *
         * Log is disabled as default.
         */
        fun enableLog(enable: Boolean) {
            enable(enable)
        }

        /**
         * The Logger specifies how this Library is logging debug information. By default [DefaultLogger]
         * is used and it can be replaced by your own custom implementation of [Logger].
         */
        fun setLogger(logger: Logger) {
            LogUtils.setLogger(logger)
        }
    }
}