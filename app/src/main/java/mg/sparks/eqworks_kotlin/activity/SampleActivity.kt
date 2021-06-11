package mg.sparks.eqworks_kotlin.activity

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import mg.henkinn.locationmanager.LocationApiService
import mg.henkinn.locationmanager.base.LocationBaseActivity
import mg.henkinn.locationmanager.configuration.*
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.ProviderType
import mg.henkinn.locationmanager.helper.logging.LogUtils.logI
import mg.henkinn.locationmanager.service.ApiResponse
import mg.henkinn.locationmanager.service.ApiServiceListener
import mg.sparks.eqworks_kotlin.R
import mg.sparks.eqworks_kotlin.SamplePresenter
import mg.sparks.eqworks_kotlin.SamplePresenter.SampleView


class SampleActivity : LocationBaseActivity(), SampleView {
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null
    private var samplePresenter: SamplePresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_display_layout)
        locationText = findViewById(R.id.locationText)
        samplePresenter = SamplePresenter(this)

        val getLocationButton = findViewById<Button>(R.id.getLocationButton)
        getLocationButton.setOnClickListener {
            getLocation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        samplePresenter!!.destroy()
    }

    override val locationConfiguration: LocationConfiguration
        get() = LocationConfiguration.Builder()
            .keepTracking(false)
            .askForPermission(
                PermissionConfiguration.Builder()
                    .rationaleMessage("Gimme the permission!")
                    .build()
            )
            .useGooglePlayServices(
                GooglePlayServicesConfiguration.Builder()
                    .locationRequest(
                        LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10 * 1000)
                            .setFastestInterval(5 * 1000)
                    )
                    .fallbackToDefault(true)
                    .askForGooglePlayServices(false)
                    .askForSettingsApi(true)
                    .failOnSettingsApiSuspended(false)
                    .ignoreLastKnowLocation(true)
                    .setWaitPeriod(10 * 1000)
                    .build()
            )
            .useDefaultProviders(
                DefaultProviderConfiguration.Builder()
                    .requiredTimeInterval((1 * 60 * 1000).toLong())
                    .requiredDistanceInterval(0)
                    .acceptableAccuracy(1.0f)
                    .acceptableTimePeriod((1 * 60 * 1000).toLong())
                    .gpsMessage("Turn on GPS?")
                    .setWaitPeriod(ProviderType.GPS, (10 * 1000).toLong())
                    .setWaitPeriod(ProviderType.NETWORK, (10 * 1000).toLong())
                    .build()
            )
            .build()


    override fun onLocationChanged(location: Location) {
        println(location)


        val apiService =
            LocationApiService("https://httpbin.org/status/200", object : ApiServiceListener {
                override fun onResponse(result: ApiResponse?) {
                    logI("result : $result")
                    Toast.makeText(
                        this@SampleActivity,
                        "Result code : ${result?.code}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled() {
                    logI("cancel")
                }
            })
        apiService.post(location)

        samplePresenter!!.onLocationChanged(location)
    }

    override fun onLocationFailed(type: FailType) {
        samplePresenter!!.onLocationFailed(type)
    }

    override fun onProcessTypeChanged(processType: ProcessType) {
        samplePresenter!!.onProcessTypeChanged(processType)
    }

    override fun onResume() {
        super.onResume()
        if (getLocationManager()!!.isWaitingForLocation
            && !getLocationManager()!!.isAnyDialogShowing
        ) {
            displayProgress()
        }
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
    }

    private fun displayProgress() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.window!!.addFlags(Window.FEATURE_NO_TITLE)
            progressDialog!!.setMessage("Getting location...")
        }
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
    }

    override var text: String?
        get() = locationText!!.text.toString()
        set(text) {
            locationText!!.text = text
        }

    override fun updateProgress(text: String?) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.setMessage(text)
        }
    }

    override fun dismissProgress() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }
}