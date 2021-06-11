package mg.sparks.eqworks_kotlin.fragment

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import mg.henkinn.locationmanager.base.LocationBaseFragment
import mg.henkinn.locationmanager.configuration.Configurations.defaultConfiguration
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.sparks.eqworks_kotlin.R
import mg.sparks.eqworks_kotlin.SamplePresenter
import mg.sparks.eqworks_kotlin.SamplePresenter.SampleView


class SampleFragment : LocationBaseFragment(), SampleView {
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null
    private var samplePresenter: SamplePresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.location_display_layout, container, false)
        locationText = view.findViewById(R.id.locationText)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (samplePresenter != null) samplePresenter!!.destroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        samplePresenter = SamplePresenter(this)
        getLocation()
    }

    override val locationConfiguration: LocationConfiguration
        get() = defaultConfiguration("Gimme the permission!", "Would you mind to turn GPS on?")

    override fun onLocationChanged(location: Location) {
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
            progressDialog = ProgressDialog(context)
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