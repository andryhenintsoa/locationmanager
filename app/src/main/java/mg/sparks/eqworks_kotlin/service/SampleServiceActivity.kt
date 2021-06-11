package mg.sparks.eqworks_kotlin.service

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.sparks.eqworks_kotlin.R
import mg.sparks.eqworks_kotlin.SamplePresenter


class SampleServiceActivity : AppCompatActivity(), SamplePresenter.SampleView {
    private var intentFilter: IntentFilter? = null
    private var samplePresenter: SamplePresenter? = null
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_display_layout)
        locationText = findViewById<TextView>(R.id.locationText)
        samplePresenter = SamplePresenter(this)
        displayProgress()
        startService(Intent(this, SampleService::class.java))
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, getIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
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

    private fun getIntentFilter(): IntentFilter {
        if (intentFilter == null) {
            intentFilter = IntentFilter()
            intentFilter!!.addAction(SampleService.ACTION_LOCATION_CHANGED)
            intentFilter!!.addAction(SampleService.ACTION_LOCATION_FAILED)
            intentFilter!!.addAction(SampleService.ACTION_PROCESS_CHANGED)
        }
        return intentFilter!!
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == SampleService.ACTION_LOCATION_CHANGED) {
                samplePresenter?.onLocationChanged(
                    intent.getParcelableExtra<Parcelable>(
                        SampleService.EXTRA_LOCATION
                    ) as Location
                )
            } else if (action == SampleService.ACTION_LOCATION_FAILED) {
                val code = intent.getIntExtra(
                    SampleService.EXTRA_FAIL_TYPE,
                    FailType.UNKNOWN.code
                )
                samplePresenter?.onLocationFailed(FailType.values().first { it.code == code }
                )
            } else if (action == SampleService.ACTION_PROCESS_CHANGED) {
                val code = intent.getIntExtra(
                    SampleService.EXTRA_PROCESS_TYPE,
                    ProcessType.GETTING_LOCATION_FROM_CUSTOM_PROVIDER.code
                )
                samplePresenter?.onProcessTypeChanged(
                    ProcessType.values().first { it.code == code }
                )
            }
        }
    }
}