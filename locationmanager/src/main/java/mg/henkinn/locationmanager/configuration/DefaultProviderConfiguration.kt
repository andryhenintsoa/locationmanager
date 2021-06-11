package mg.henkinn.locationmanager.configuration

import mg.henkinn.locationmanager.constants.ProviderType
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider
import mg.henkinn.locationmanager.provider.dialogprovider.SimpleMessageDialogProvider

open class DefaultProviderConfiguration private constructor(builder: Builder) {


    private val requiredTimeInterval: Long = builder.requiredTimeInterval
    private val requiredDistanceInterval: Long = builder.requiredDistanceInterval
    private val acceptableAccuracy: Float = builder.acceptableAccuracy
    private val acceptableTimePeriod: Long = builder.acceptableTimePeriod
    private val gpsWaitPeriod: Long = builder.gpsWaitPeriod
    private val networkWaitPeriod: Long = builder.networkWaitPeriod
    private val gpsDialogProvider: DialogProvider? = builder.gpsDialogProvider

    fun newBuilder(): Builder {
        return Builder()
            .requiredTimeInterval(requiredTimeInterval)
            .requiredDistanceInterval(requiredDistanceInterval)
            .acceptableAccuracy(acceptableAccuracy)
            .acceptableTimePeriod(acceptableTimePeriod)
            .setWaitPeriod(ProviderType.GPS, gpsWaitPeriod)
            .setWaitPeriod(ProviderType.NETWORK, networkWaitPeriod)
            .gpsDialogProvider(gpsDialogProvider)
    }

    // region Getters
    open fun requiredTimeInterval(): Long {
        return requiredTimeInterval
    }

    open fun requiredDistanceInterval(): Long {
        return requiredDistanceInterval
    }

    open fun acceptableAccuracy(): Float {
        return acceptableAccuracy
    }

    open fun acceptableTimePeriod(): Long {
        return acceptableTimePeriod
    }

    open fun askForEnableGPS(): Boolean {
        return gpsDialogProvider != null
    }

    open fun gpsDialogProvider(): DialogProvider? {
        return gpsDialogProvider
    }

    open fun gpsWaitPeriod(): Long {
        return gpsWaitPeriod
    }

    open fun networkWaitPeriod(): Long {
        return networkWaitPeriod
    }

    // endregion
    class Builder {
        internal var requiredTimeInterval: Long = Defaults.LOCATION_INTERVAL
        internal var requiredDistanceInterval: Long = Defaults.LOCATION_DISTANCE_INTERVAL
        internal var acceptableAccuracy: Float = Defaults.MIN_ACCURACY
        internal var acceptableTimePeriod: Long = Defaults.TIME_PERIOD
        internal var gpsWaitPeriod: Long = Defaults.WAIT_PERIOD
        internal var networkWaitPeriod: Long = Defaults.WAIT_PERIOD
        internal var gpsDialogProvider: DialogProvider? = null
        internal var gpsMessage: String = Defaults.EMPTY_STRING

        /**
         * TimeInterval will be used while getting location from default location providers
         * It will define in which period updates need to be delivered and will be used only when
         * [LocationConfiguration.keepTracking] is set to true.
         * Default is [Defaults.LOCATION_INTERVAL]
         */
        fun requiredTimeInterval(requiredTimeInterval: Long): Builder {
            if (requiredTimeInterval < 0) {
                throw IllegalArgumentException("requiredTimeInterval cannot be set to negative value.")
            }
            this.requiredTimeInterval = requiredTimeInterval
            return this
        }

        /**
         * DistanceInterval will be used while getting location from default location providers
         * It will define in which distance changes that we should receive an update and will be used only when
         * [LocationConfiguration.keepTracking] is set to true.
         * Default is [Defaults.LOCATION_DISTANCE_INTERVAL]
         */
        fun requiredDistanceInterval(requiredDistanceInterval: Long): Builder {
            if (requiredDistanceInterval < 0) {
                throw IllegalArgumentException("requiredDistanceInterval cannot be set to negative value.")
            }
            this.requiredDistanceInterval = requiredDistanceInterval
            return this
        }

        /**
         * Minimum Accuracy that you seek location to be
         * Default is [Defaults.MIN_ACCURACY]
         */
        fun acceptableAccuracy(acceptableAccuracy: Float): Builder {
            if (acceptableAccuracy < 0) {
                throw IllegalArgumentException("acceptableAccuracy cannot be set to negative value.")
            }
            this.acceptableAccuracy = acceptableAccuracy
            return this
        }

        /**
         * Indicates time period that can be count as usable location,
         * this needs to be considered such as "last 5 minutes"
         * Default is [Defaults.TIME_PERIOD]
         */
        fun acceptableTimePeriod(acceptableTimePeriod: Long): Builder {
            if (acceptableTimePeriod < 0) {
                throw IllegalArgumentException("acceptableTimePeriod cannot be set to negative value.")
            }
            this.acceptableTimePeriod = acceptableTimePeriod
            return this
        }

        /**
         * Indicates what to display to user while asking to turn GPS on.
         * If you do not set this, user will not be asked to enable GPS.
         */
        fun gpsMessage(gpsMessage: String): Builder {
            this.gpsMessage = gpsMessage
            return this
        }

        /**
         * If you need to display a custom dialog to ask user to enable GPS, you can provide your own
         * implementation of [DialogProvider] and manager will use that implementation to display the dialog.
         * Important, if you set your own implementation, please make sure to handle gpsMessage as well.
         * Because [DefaultProviderConfiguration.Builder.gpsMessage] will be ignored in that case.
         *
         * If you don't specify any dialogProvider implementation [SimpleMessageDialogProvider] will be used with
         * given [DefaultProviderConfiguration.Builder.gpsMessage]
         */
        fun gpsDialogProvider(dialogProvider: DialogProvider?): Builder {
            gpsDialogProvider = dialogProvider
            return this
        }

        /**
         * Indicates waiting time period before switching to next possible provider.
         * Possible to set provider wait periods separately by passing providerType as one of the
         * [ProviderType] values.
         * Default values are [Defaults.WAIT_PERIOD]
         */
        fun setWaitPeriod(providerType: ProviderType, milliseconds: Long): Builder {
            if (milliseconds < 0) {
                throw IllegalArgumentException("waitPeriod cannot be set to negative value.")
            }
            when (providerType) {
                ProviderType.GOOGLE_PLAY_SERVICES -> {
                    throw IllegalStateException(
                        "GooglePlayServices waiting time period should be set on "
                                + "GooglePlayServicesConfiguration"
                    )
                }
                ProviderType.NETWORK -> {
                    networkWaitPeriod = milliseconds
                }
                ProviderType.GPS -> {
                    gpsWaitPeriod = milliseconds
                }
                ProviderType.DEFAULT_PROVIDERS -> {
                    gpsWaitPeriod = milliseconds
                    networkWaitPeriod = milliseconds
                }
                ProviderType.NONE -> {
                }
            }
            return this
        }

        fun build(): DefaultProviderConfiguration {
            if (gpsDialogProvider == null && gpsMessage.isNotEmpty()) {
                gpsDialogProvider = SimpleMessageDialogProvider(gpsMessage)
            }
            return DefaultProviderConfiguration(this)
        }
    }
}