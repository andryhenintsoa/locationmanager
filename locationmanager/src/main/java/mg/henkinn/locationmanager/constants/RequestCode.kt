package mg.henkinn.locationmanager.constants

enum class RequestCode(val code: Int) {

    RUNTIME_PERMISSION(23),
    GOOGLE_PLAY_SERVICES(24),
    GPS_ENABLE(25),
    SETTINGS_API(26)
}