package mg.sparks.eqworks_kotlin

import android.app.Application
import mg.henkinn.locationmanager.LocationManager

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocationManager.enableLog(true)
    }
}