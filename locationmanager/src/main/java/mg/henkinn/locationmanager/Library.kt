package mg.henkinn.locationmanager

import android.content.Context

public data class LocationEvent(val lat: Float, val lon: Float, val time: Long, val ext: String)

interface Library {

    var appContext: Context
    var baseUrl: String

    fun setup(): Boolean {
        return true
    }

    fun log(event: LocationEvent) {
        // POST to API Server
    }

    class Builder {
        var appContext: Context? = null;
        var baseUrl: String? = null;

        fun appContext(appContext: Context) = apply {
            this.appContext = appContext
        }

        fun baseUrl(baseUrl: String) = apply {
            this.baseUrl = baseUrl
        }

        fun build(): Library {

            requireNotNull(appContext) {
                "`appContext` cannot be null"
            }
            requireNotNull(baseUrl) {
                "`baseUrl` cannot be null"
            }

            return LibraryImpl(appContext!!, baseUrl!!)
        }

    }

    companion object {
        lateinit var instance: Library internal set
    }
}

/**
 * Implementation of the [Graph]
 */
private class LibraryImpl(
    override var appContext: Context, override var baseUrl: String,
) : Library {

    init {
        Library.instance = this
    }
}

