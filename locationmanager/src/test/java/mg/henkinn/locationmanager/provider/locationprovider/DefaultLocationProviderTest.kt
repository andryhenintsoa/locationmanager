package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.location.LocationManager
import mg.henkinn.locationmanager.configuration.DefaultProviderConfiguration
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.UpdateRequest
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask
import mg.henkinn.locationmanager.listener.LocationListener
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider
import mg.henkinn.locationmanager.safeAny
import mg.henkinn.locationmanager.safeAnyString
import mg.henkinn.locationmanager.view.ContextProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DefaultLocationProviderTest {
    companion object {
        private const val GPS_PROVIDER: String = LocationManager.GPS_PROVIDER
        private const val NETWORK_PROVIDER: String = LocationManager.NETWORK_PROVIDER
        private val DUMMY_LOCATION: Location = Location("")
    }

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var dialog: Dialog

    @Mock
    lateinit var continuousTask: ContinuousTask

    @Mock
    lateinit var updateRequest: UpdateRequest

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var defaultProviderConfiguration: DefaultProviderConfiguration

    @Mock
    lateinit var dialogProvider: DialogProvider

    @Mock
    lateinit var defaultLocationSource: DefaultLocationSource
    private var defaultLocationProvider: DefaultLocationProvider? = null

    @Before
    fun setUp() {
        `when`(locationConfiguration.defaultProviderConfiguration()).thenReturn(
            defaultProviderConfiguration
        )
        `when`(defaultProviderConfiguration.gpsDialogProvider()).thenReturn(dialogProvider)
        `when`(dialogProvider.getDialog(safeAny(Context::class.java))).thenReturn(dialog)
        `when`(contextProcessor.activity).thenReturn(activity)
        `when`(defaultLocationSource.getProviderSwitchTask()).thenReturn(continuousTask)
        `when`(defaultLocationSource.getUpdateRequest()).thenReturn(updateRequest)

        defaultLocationProvider = spy(DefaultLocationProvider())
        defaultLocationProvider!!.setDefaultLocationSource(defaultLocationSource)
        defaultLocationProvider!!.configure(
            contextProcessor,
            locationConfiguration,
            locationListener
        )
    }

    @Test
    fun configureShouldInvokeInitialize() {
        val provider: DefaultLocationProvider = spy(DefaultLocationProvider())
        provider.configure(defaultLocationProvider!!)
        verify(provider).initialize()
    }

    @Test
    fun onDestroyShouldRemoveInstances() {
        defaultLocationProvider!!.onDestroy()
        verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider!!)
        verify(defaultLocationSource).removeSwitchTask()
        verify(defaultLocationSource).removeUpdateRequest()
    }

    @Test
    fun cancelShouldStopTasks() {
        defaultLocationProvider!!.cancel()
        verify(updateRequest).release()
        verify(continuousTask).stop()
    }

    @Test
    fun onPauseShouldPauseTasks() {
        defaultLocationProvider!!.onPause()
        verify(updateRequest).release()
        verify(continuousTask).pause()
    }

    @Test
    fun onResumeShouldResumeUpdateRequest() {
        defaultLocationProvider!!.onResume()
        verify(updateRequest).run()
    }

    @Test
    fun onResumeShouldResumeSwitchTaskWhenLocationIsStillRequired() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onResume()
        verify(continuousTask).resume()
    }

    @Test
    fun onResumeDialogShouldDismissWhenDialogIsOnAndGPSIsActivated() {
        defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
        `when`(dialog.isShowing).thenReturn(true)
        enableLocationProvider()
        defaultLocationProvider!!.onResume()
        verify(dialog).dismiss()
        verify(defaultLocationProvider)?.onGPSActivated()
    }

    @Test
    fun onResumeDialogShouldNotDismissedWhenGPSNotActivated() {
        defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
        `when`(dialog.isShowing).thenReturn(true)
        disableLocationProvider()
        defaultLocationProvider!!.onResume()
        verify(dialog, never()).dismiss()
    }

    @Test
    fun isDialogShowingShouldReturnFalseWhenGPSDialogIsNotOn() {
        assertThat(defaultLocationProvider!!.isDialogShowing).isFalse
    }

    // to get dialog initialized
    @Test
    fun isDialogShowingShouldReturnTrueWhenGPSDialogIsOn() {
        defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
        `when`(dialog.isShowing).thenReturn(true)
        assertThat(defaultLocationProvider!!.isDialogShowing).isTrue
    }

    @Test
    fun onActivityResultShouldCallOnGPSActivated() {
        enableLocationProvider()
        defaultLocationProvider!!.onActivityResult(RequestCode.GPS_ENABLE, -1, null)
        verify(defaultLocationProvider)?.onGPSActivated()
    }

    @Test
    fun onActivityResultShouldCallGetLocationByNetworkWhenGPSIsNotEnabled() {
        disableLocationProvider()
        defaultLocationProvider!!.onActivityResult(RequestCode.GPS_ENABLE, -1, null)
        verify(defaultLocationProvider)?.getLocationByNetwork()
    }

    @Test
    fun shouldSetWaitingTrue() {
        enableLocationProvider()
        assertThat(defaultLocationProvider!!.isWaiting).isFalse
        defaultLocationProvider!!.get()
        assertThat(defaultLocationProvider!!.isWaiting).isTrue
    }

    @Test
    fun shouldAskForLocationWithGPSProviderWhenItIsEnabled() {
        enableLocationProvider()
        defaultLocationProvider!!.get()
        verify(defaultLocationProvider)?.askForLocation(GPS_PROVIDER)
    }

    @Test
    fun shouldAskForEnableGPSWhenGPSIsNotEnabledButRequiredByConfigurationToAskForIt() {
        disableLocationProvider()
        `when`(defaultProviderConfiguration.askForEnableGPS()).thenReturn(true)
        defaultLocationProvider!!.get()
        verify(defaultLocationProvider)?.askForEnableGPS()
    }

    @Test
    fun shouldGetLocationByNetworkWhenGPSNotRequiredToAsk() {
        disableLocationProvider()
        `when`(defaultProviderConfiguration.askForEnableGPS()).thenReturn(false)
        defaultLocationProvider!!.get()
        verify(defaultLocationProvider)?.getLocationByNetwork()
    }

    @Test
    fun shouldGetLocationByNetworkWhenGPSNotEnabledAndThereIsNoActivity() {
        disableLocationProvider()
        `when`(defaultProviderConfiguration.askForEnableGPS()).thenReturn(true)
        `when`(contextProcessor.activity).thenReturn(null)
        defaultLocationProvider!!.get()
        verify(defaultLocationProvider)?.getLocationByNetwork()
    }

    @Test
    fun askForEnableGPSShouldShowDialog() {
        defaultLocationProvider!!.askForEnableGPS()
        verify(dialogProvider).dialogListener = defaultLocationProvider
        verify(dialogProvider).getDialog(activity)
        verify(dialog).show()
    }

    @Test
    fun onGPSActivatedShouldAskForLocation() {
        defaultLocationProvider!!.onGPSActivated()
        verify(defaultLocationProvider)?.askForLocation(GPS_PROVIDER)
    }

    @Test
    fun locationByNetworkShouldAskForLocationWhenNetworkIsAvailable() {
        enableLocationProvider()
        defaultLocationProvider!!.getLocationByNetwork()
        verify(defaultLocationProvider)?.askForLocation(LocationManager.NETWORK_PROVIDER)
    }

    @Test
    fun locationByNetworkShouldFailWhenNetworkIsNotAvailable() {
        disableLocationProvider()
        defaultLocationProvider!!.getLocationByNetwork()
        verify(locationListener).onLocationFailed(FailType.NETWORK_NOT_AVAILABLE)
    }

    @Test
    fun askForLocationShouldStopSwitchTasks() {
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        verify(continuousTask).stop()
    }

    @Test
    fun askForLocationShouldCheckLastKnowLocation() {
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        verify(defaultLocationProvider)?.checkForLastKnowLocation()
    }

    @Test
    fun askForLocationShouldNotifyProcessChangeRequestLocationUpdateDelayTaskWhenLastLocationIsNotSufficient() {
        val oneSecond: Long = 1000
        `when`(defaultLocationSource.getProviderSwitchTask()).thenReturn(continuousTask)
        `when`(defaultProviderConfiguration.gpsWaitPeriod()).thenReturn(oneSecond)
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        verify(defaultLocationProvider)?.notifyProcessChange()
        verify(defaultLocationProvider)?.requestUpdateLocation()
        verify(continuousTask).delayed(oneSecond)
    }

    @Test
    fun askForLocationShouldNotifyProcessChangeAndRequestLocationUpdateWhenKeepTrackingIsTrue() {
        val location = Location(GPS_PROVIDER)
        `when`(defaultProviderConfiguration.acceptableAccuracy()).thenReturn(1f)
        `when`(defaultProviderConfiguration.acceptableTimePeriod()).thenReturn(1L)
        `when`(defaultLocationSource.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location)
        `when`(defaultLocationSource.isLocationSufficient(location, 1L, 1f)).thenReturn(true)
        `when`(locationConfiguration.keepTracking()).thenReturn(true)
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        verify(defaultLocationProvider)?.notifyProcessChange()
        verify(defaultLocationProvider)?.requestUpdateLocation()
    }

    @Test
    fun checkForLastKnownLocationShouldReturnFalse() {
        assertThat(defaultLocationProvider!!.checkForLastKnowLocation()).isFalse
    }

    @Test
    fun checkForLastKnownLocationShouldCallOnLocationReceivedAndReturnTrueWhenSufficient() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        val location = Location(GPS_PROVIDER)
        `when`(defaultProviderConfiguration.acceptableAccuracy()).thenReturn(1f)
        `when`(defaultProviderConfiguration.acceptableTimePeriod()).thenReturn(1L)
        `when`(defaultLocationSource.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location)
        `when`(defaultLocationSource.isLocationSufficient(location, 1L, 1f)).thenReturn(true)
        assertThat(defaultLocationProvider!!.checkForLastKnowLocation()).isTrue
        verify(locationListener).onLocationChanged(location)
    }

    @Test
    fun notifyProcessChangeShouldNotifyWithCorrespondingTypeForProvider() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.notifyProcessChange()
        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER)
        defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
        defaultLocationProvider!!.notifyProcessChange()
        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER)
    }

    @Test
    fun requestUpdateLocationShouldRunUpdateLocationTaskWithCurrentProvider() {
        val timeInterval: Long = 100
        val distanceInterval: Long = 200
        `when`(defaultProviderConfiguration.requiredTimeInterval()).thenReturn(timeInterval)
        `when`(defaultProviderConfiguration.requiredDistanceInterval()).thenReturn(
            distanceInterval
        )
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.requestUpdateLocation()
        verify(updateRequest).run(GPS_PROVIDER, timeInterval, distanceInterval.toFloat())
    }

    @Test
    fun waitPeriodShouldReturnCorrespondingTimeForProvider() {
        val gpsWaitPeriod: Long = 100
        val networkWaitPeriod: Long = 200
        `when`(defaultProviderConfiguration.gpsWaitPeriod()).thenReturn(gpsWaitPeriod)
        `when`(defaultProviderConfiguration.networkWaitPeriod()).thenReturn(networkWaitPeriod)
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        assertThat(defaultLocationProvider!!.waitPeriod).isEqualTo(gpsWaitPeriod)
        defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
        assertThat(defaultLocationProvider!!.waitPeriod).isEqualTo(networkWaitPeriod)
    }

    @Test
    fun onLocationReceivedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onLocationReceived(DUMMY_LOCATION)
        verify(locationListener).onLocationChanged(DUMMY_LOCATION)
        assertThat(defaultLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLocationFailedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onLocationFailed(FailType.UNKNOWN)
        verify(locationListener).onLocationFailed(FailType.UNKNOWN)
        assertThat(defaultLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLocationChangedShouldPassLocationToReceived() {
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(defaultLocationProvider)?.onLocationReceived(DUMMY_LOCATION)
    }

    @Test
    fun onLocationChangedShouldStopSwitchTask() {
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(continuousTask).stop()
    }

    @Test
    fun onLocationChangedShouldNotStopSwitchTaskIfSwitchTaskIsRemoved() {
        `when`(defaultLocationSource.switchTaskIsRemoved()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(continuousTask, never()).stop()
    }

    @Test
    fun onLocationChangedShouldReturnIfUpdateRequestIsRemoved() {
        `when`(defaultLocationSource.updateRequestIsRemoved()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(defaultLocationProvider, never())?.onLocationReceived(DUMMY_LOCATION)
    }

    @Test
    fun onLocationChangedShouldRemoveUpdatesWhenKeepTrackingFalse() {
        `when`(locationConfiguration.keepTracking()).thenReturn(false)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider!!)
        verify(updateRequest).release()
    }

    @Test
    fun onLocationChangedShouldNotRemoveUpdatesWhenKeepTrackingTrue() {
        `when`(locationConfiguration.keepTracking()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        verify(defaultLocationSource, never()).removeLocationUpdates(defaultLocationProvider!!)
        verify(updateRequest, never()).release()
    }

    @Test
    fun onStatusChangedShouldRedirectToListener() {
        defaultLocationProvider!!.onStatusChanged(GPS_PROVIDER, 1, null)
        verify(locationListener).onStatusChanged(GPS_PROVIDER, 1, null)
    }

    @Test
    fun onProviderEnabledShouldRedirectToListener() {
        defaultLocationProvider!!.onProviderEnabled(GPS_PROVIDER)
        verify(locationListener).onProviderEnabled(GPS_PROVIDER)
    }

    @Test
    fun onProviderDisabledShouldRedirectToListener() {
        defaultLocationProvider!!.onProviderDisabled(GPS_PROVIDER)
        verify(locationListener).onProviderDisabled(GPS_PROVIDER)
    }

    @Test
    fun runScheduledTaskShouldReleaseUpdateRequest() {
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        verify(updateRequest).release()
    }

    @Test
    fun runScheduledTaskShouldGetLocationByNetworkWhenCurrentProviderIsGPS() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        verify(defaultLocationProvider)?.getLocationByNetwork()
    }

    @Test
    fun runScheduledTaskShouldFailWithTimeoutWhenCurrentProviderIsNetwork() {
        defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        verify(locationListener).onLocationFailed(FailType.TIMEOUT)
    }

    @Test
    fun onPositiveButtonClickShouldFailWhenThereIsNoActivityOrFragment() {
        `when`(contextProcessor.activity).thenReturn(null)
        defaultLocationProvider!!.onPositiveButtonClick()
        verify(locationListener).onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE)
    }

    @Test
    fun onNegativeButtonClickShouldGetLocationByNetwork() {
        defaultLocationProvider!!.onNegativeButtonClick()
        verify(defaultLocationProvider)?.getLocationByNetwork()
    }

    private fun enableLocationProvider() {
        `when`(defaultLocationSource.isProviderEnabled(safeAnyString())).thenReturn(true)
    }

    private fun disableLocationProvider() {
        `when`(defaultLocationSource.isProviderEnabled(safeAnyString())).thenReturn(false)
    }
}