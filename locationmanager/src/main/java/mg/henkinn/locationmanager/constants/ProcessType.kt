package mg.henkinn.locationmanager.constants

enum class ProcessType(val code: Int) {


    /**
     * This type will be emitted when application doesn't have required permissions yet,
     * and library starts the process to ask for them. If application already has the permissions,
     * this will not be emitted.
     */
    ASKING_PERMISSIONS(1),

    /**
     * This type will be emitted when GooglePlayServices is available on device and possible to ask for location update,
     * otherwise it will not be emitted.
     */
    GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES(2),

    /**
     * This type will be emitted as soon as library asks Location update with GPS provider,
     * otherwise it will not be emitted.
     */
    GETTING_LOCATION_FROM_GPS_PROVIDER(3),

    /**
     * This type will be emitted as soon as library asks Location update with Network provider,
     * otherwise it will not be emitted.
     */
    GETTING_LOCATION_FROM_NETWORK_PROVIDER(4),

    /**
     * This type will never be emitted by the library.
     * It is defined in case of a custom Location Provider set to LocationManager.
     */
    GETTING_LOCATION_FROM_CUSTOM_PROVIDER(5)
}