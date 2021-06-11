package mg.henkinn.locationmanager.service

import okhttp3.*

class ApiResponse(response: Response) {
    /** Returns the HTTP status message. */
    val message: String

    /** Returns the HTTP status code. */
    val code: Int

    /** Returns the HTTP headers. */
    val headers: Iterable<Pair<String, String>>

    /**
     * Returns the response body
     */
    val body: String?

    init {
        this.code = response.code()
        this.message = response.message()
        val mutableList = mutableListOf<Pair<String, String>>()
        response.headers().names().forEach {
            mutableList.add(Pair(it, response.header(it)!!))
        }
        this.headers = mutableList
        this.body = response.body()?.string()
    }
}