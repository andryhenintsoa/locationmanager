package mg.henkinn.locationmanager.service

interface ApiServiceListener {
    fun onResponse(result: ApiResponse?)
    fun onCancelled()
}