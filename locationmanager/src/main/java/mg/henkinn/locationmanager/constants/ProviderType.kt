package mg.henkinn.locationmanager.constants

enum class ProviderType(val code:Int) {

    NONE(0),
    GOOGLE_PLAY_SERVICES(1),
    GPS(2),
    NETWORK(3),
    DEFAULT_PROVIDERS(4) // Covers both GPS and NETWORK
}