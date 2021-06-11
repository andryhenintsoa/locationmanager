package mg.henkinn.locationmanager.configuration

import mg.henkinn.locationmanager.provider.permissionprovider.StubPermissionProvider


open class LocationConfiguration private constructor(builder: Builder) {
    private val keepTracking: Boolean
    private val permissionConfiguration: PermissionConfiguration
    private val googlePlayServicesConfiguration: GooglePlayServicesConfiguration?
    private val defaultProviderConfiguration: DefaultProviderConfiguration?

    init {
        keepTracking = builder.keepTracking
        permissionConfiguration = builder.permissionConfiguration!!
        googlePlayServicesConfiguration = builder.googlePlayServicesConfiguration
        defaultProviderConfiguration = builder.defaultProviderConfiguration
    }

    open fun test() : Boolean = true

    fun newBuilder(): Builder {
        return Builder()
            .keepTracking(keepTracking)
            .askForPermission(permissionConfiguration)
            .useGooglePlayServices(googlePlayServicesConfiguration)
            .useDefaultProviders(defaultProviderConfiguration)
    }

    // region Getters
    open fun keepTracking(): Boolean {
        return keepTracking
    }

    open fun permissionConfiguration(): PermissionConfiguration {
        return permissionConfiguration
    }

    open fun googlePlayServicesConfiguration(): GooglePlayServicesConfiguration? {
        return googlePlayServicesConfiguration
    }

    open fun defaultProviderConfiguration(): DefaultProviderConfiguration? {
        return defaultProviderConfiguration
    }

    // endregion
    class Builder {
        internal var keepTracking: Boolean = Defaults.KEEP_TRACKING
        internal var permissionConfiguration: PermissionConfiguration? = null
        internal var googlePlayServicesConfiguration: GooglePlayServicesConfiguration? = null
        internal var defaultProviderConfiguration: DefaultProviderConfiguration? = null

        /**
         * If you need to keep receiving location updates, then you need to set this as true.
         * Otherwise manager will be aborted after any location received.
         * Default is False.
         */
        fun keepTracking(keepTracking: Boolean): Builder {
            this.keepTracking = keepTracking
            return this
        }

        /**
         * This configuration is required in order to configure Permission Request process.
         * If this is not set, then no permission will be requested from user and
         * if [Defaults.LOCATION_PERMISSIONS] permissions are not granted already,
         * then getting location will fail silently.
         */
        fun askForPermission(permissionConfiguration: PermissionConfiguration?): Builder {
            this.permissionConfiguration = permissionConfiguration
            return this
        }

        /**
         * This configuration is required in order to configure GooglePlayServices Api.
         * If this is not set, then GooglePlayServices will not be used.
         */
        fun useGooglePlayServices(googlePlayServicesConfiguration: GooglePlayServicesConfiguration?): Builder {
            this.googlePlayServicesConfiguration = googlePlayServicesConfiguration
            return this
        }

        /**
         * This configuration is required in order to configure Default Location Providers.
         * If this is not set, then they will not be used.
         */
        fun useDefaultProviders(defaultProviderConfiguration: DefaultProviderConfiguration?): Builder {
            this.defaultProviderConfiguration = defaultProviderConfiguration
            return this
        }

        fun build(): LocationConfiguration {
            if (googlePlayServicesConfiguration == null && defaultProviderConfiguration == null) {
                throw IllegalStateException(
                    "You need to specify one of the provider configurations."
                            + " Please see GooglePlayServicesConfiguration and DefaultProviderConfiguration"
                )
            }
            if (permissionConfiguration == null) {
                permissionConfiguration = PermissionConfiguration.Builder()
                    .permissionProvider(StubPermissionProvider())
                    .build()
            }
            return LocationConfiguration(this)
        }
    }
}