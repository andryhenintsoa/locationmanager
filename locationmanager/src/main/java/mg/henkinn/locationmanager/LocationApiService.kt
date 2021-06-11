package mg.henkinn.locationmanager

import android.location.Location
import android.os.AsyncTask
import mg.henkinn.locationmanager.service.ApiResponse
import mg.henkinn.locationmanager.service.ApiServiceListener
import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.*

open class LocationApiService(private var url: String, var listener: ApiServiceListener) :
    AsyncTask<Any, Int, ApiResponse>() {

    private var client = OkHttpClient()


    fun post(
        location: Location?,
        time: Long? = null,
        otherParameters: Map<String, Any>? = null
    ) {
        execute(location, time, otherParameters)
    }

    internal fun postSync(
        location: Location?,
        time: Long? = null,
        otherParameters: Map<String, Any>? = null
    ): ApiResponse {
        val jsonBuilder = JsonBuilder()

        if (location == null) {
            jsonBuilder.add("latitude", 0)
            jsonBuilder.add("longitude", 0)
        } else {
            jsonBuilder.add("latitude", location.latitude)
            jsonBuilder.add("longitude", location.longitude)
        }

        jsonBuilder.add("time", time ?: Date().time)

        if (otherParameters != null) {
            jsonBuilder.addAll(otherParameters)
        }

        return postSync(url, jsonBuilder.build())
    }

    @Throws(IOException::class)
    private fun postSync(url: String, json: String?): ApiResponse {

        if (json == null) throw IOException()
        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            return ApiResponse(response)
        }
    }

    override fun doInBackground(vararg args: Any?): ApiResponse {
        return postSync(args[0] as Location?, args[1] as Long?, args[2] as? Map<String, Any>?)
    }

    override fun onPostExecute(result: ApiResponse?) {
        super.onPostExecute(result)

        when (result) {
            null -> {
                listener.onResponse(null)
            }
            else -> {
                listener.onResponse(result)
            }
        }
    }

    override fun onCancelled(result: ApiResponse?) {
        super.onCancelled(result)
        listener.onCancelled()
    }

    companion object {
        val JSON: MediaType = MediaType.get("application/json; charset=utf-8")
    }

    class JsonBuilder {

        val content = mutableMapOf<String, Any>()

        fun add(key: String, value: Any) {
            content[key] = value
        }

        fun addAll(from: Map<String, Any>) {
            content.putAll(from)
        }

        fun build(): String {
            val buffer = StringBuffer()

            for ((k, v) in content) {
                if (buffer.isNotEmpty()) buffer.append(" ,")

                buffer.append("'$k':")
                if (v is Number) {
                    buffer.append("$v")
                } else {
                    buffer.append("'$v'")
                }
            }

            buffer.insert(0, "{")
            buffer.append("}")

            return buffer.toString()
        }

    }

}