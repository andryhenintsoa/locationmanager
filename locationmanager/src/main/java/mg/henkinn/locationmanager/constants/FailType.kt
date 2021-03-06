package mg.henkinn.locationmanager.constants

enum class FailType(val code: Int) {
    UNKNOWN(-1),
    TIMEOUT(1),
    PERMISSION_DENIED(2),
    NETWORK_NOT_AVAILABLE(3),
    GOOGLE_PLAY_SERVICES_NOT_AVAILABLE(4),
    GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG(6),
    GOOGLE_PLAY_SERVICES_SETTINGS_DENIED(7),
    VIEW_DETACHED(8),
    VIEW_NOT_REQUIRED_TYPE(9)
}