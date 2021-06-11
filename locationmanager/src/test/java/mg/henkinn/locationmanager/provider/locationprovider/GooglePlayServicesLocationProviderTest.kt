package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.Location
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import mg.henkinn.locationmanager.configuration.GooglePlayServicesConfiguration
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.fakes.FakeSimpleTask
import mg.henkinn.locationmanager.listener.FallbackListener
import mg.henkinn.locationmanager.listener.LocationListener
import mg.henkinn.locationmanager.safeAny
import mg.henkinn.locationmanager.view.ContextProcessor
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class GooglePlayServicesLocationProviderTest {
    @Mock
    internal lateinit var mockedSource: GooglePlayServicesLocationSource

    @Mock
    lateinit var location: Location

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var googlePlayServicesConfiguration: GooglePlayServicesConfiguration

    @Mock
    lateinit var fallbackListener: FallbackListener
    private lateinit var googlePlayServicesLocationProvider: GooglePlayServicesLocationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        googlePlayServicesLocationProvider =
            spy(GooglePlayServicesLocationProvider(fallbackListener))
        googlePlayServicesLocationProvider.configure(
            contextProcessor,
            locationConfiguration,
            locationListener
        )
        googlePlayServicesLocationProvider.setDispatcherLocationSource(mockedSource)
        `when`(locationConfiguration.googlePlayServicesConfiguration())
            .thenReturn(googlePlayServicesConfiguration)
        `when`(contextProcessor.context).thenReturn(context)
        `when`(contextProcessor.activity).thenReturn(activity)
    }

    @Test
    fun onResumeShouldNotRequestLocationUpdateWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue()
        googlePlayServicesLocationProvider.onResume()
        verify(mockedSource, never()).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldNotRequestLocationUpdateWhenLocationIsAlreadyProvidedAndNotRequiredToKeepTracking() {
        `when`(locationConfiguration.keepTracking()).thenReturn(false)
        googlePlayServicesLocationProvider.onResume()
        verify(mockedSource, never()).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldRequestLocationUpdateWhenLocationIsNotYetProvided() {
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider.isWaiting = true
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldRequestLocationUpdateWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        googlePlayServicesLocationProvider.isWaiting = true
        googlePlayServicesLocationProvider.onResume()
        verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun onPauseShouldNotRemoveLocationUpdatesWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue()
        googlePlayServicesLocationProvider.onPause()
        verify(mockedSource, never()).requestLocationUpdate()
        verify(mockedSource, never()).removeLocationUpdates()
    }

    @Test
    fun onPauseShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider.onPause()
        verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onDestroyShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider.onDestroy()
        verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun isDialogShownShouldReturnFalseWhenSettingsApiDialogIsNotShown() {
        Assertions.assertThat(googlePlayServicesLocationProvider.isDialogShowing).isFalse
    }

    @Test
    fun isDialogShownShouldReturnTrueWhenSettingsApiDialogShown() {
        makeSettingsDialogIsOnTrue()
        Assertions.assertThat(googlePlayServicesLocationProvider.isDialogShowing).isTrue
    }

    @Test
    fun shouldSetWaitingTrue() {
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isFalse
        googlePlayServicesLocationProvider.get()
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isTrue
    }

    @Test
    fun shouldFailWhenThereIsNoContext() {
        `when`(contextProcessor.context).thenReturn(null)
        googlePlayServicesLocationProvider.get()
        verify(locationListener).onLocationFailed(FailType.VIEW_DETACHED)
    }

    @Test
    fun shouldRequestLastLocation() {
        googlePlayServicesLocationProvider.get()
        verify(mockedSource).requestLastLocation()
    }

    @Test
    fun shouldNotRequestLastLocationWhenIgnore() {
        `when`(googlePlayServicesConfiguration.ignoreLastKnowLocation())
            .thenReturn(true)
        googlePlayServicesLocationProvider.get()
        verify(mockedSource, never()).requestLastLocation()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        `when`(locationConfiguration.keepTracking()).thenReturn(false)
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider, never()).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldCallRequestLocationUpdateWhenLastLocationIsNull() {
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider).locationRequired()
        verify(googlePlayServicesLocationProvider).requestLocationUpdate()
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isFalse
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldCallLocationRequiredWhenLastKnowIsNotAvailable() {
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldNotifyOnLocationChangedWhenLocationIsAvailable() {
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider).onLocationChanged(location)
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldInvokeLocationRequiredWhenKeepTrackingIsTrue() {
        `when`(locationConfiguration.keepTracking()).thenReturn(true)
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider).onLocationChanged(location)
        verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldInvokeRequestLocationFalseWhenLastKnownLocationIsNull() {
        val lastLocationTask: FakeSimpleTask<Location> = FakeSimpleTask()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask)
        verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun cancelShouldRemoveLocationRequestWhenInvokeCancel() {
        googlePlayServicesLocationProvider.cancel()
        verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onActivityResultShouldSetDialogShownToFalse() {
        makeSettingsDialogIsOnTrue()
        Assertions.assertThat(googlePlayServicesLocationProvider.isDialogShowing).isTrue
        googlePlayServicesLocationProvider.onActivityResult(RequestCode.SETTINGS_API, -1, null)
        Assertions.assertThat(googlePlayServicesLocationProvider.isDialogShowing).isFalse
    }

    @Test
    fun onActivityResultShouldRequestLocationUpdateWhenResultIsOk() {
        googlePlayServicesLocationProvider.onActivityResult(
            RequestCode.SETTINGS_API,
            Activity.RESULT_OK,
            null
        )
        verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun onActivityResultShouldCallSettingsApiFailWhenResultIsNotOk() {
        googlePlayServicesLocationProvider.onActivityResult(
            RequestCode.SETTINGS_API,
            Activity.RESULT_CANCELED,
            null
        )
        verify(googlePlayServicesLocationProvider)
            .settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
    }

    @Test
    fun onLocationChangedShouldNotifyListener() {
        googlePlayServicesLocationProvider.onLocationChanged(location)
        verify(locationListener).onLocationChanged(location)
        verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onLocationChangedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider.isWaiting = true
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isTrue
        googlePlayServicesLocationProvider.onLocationChanged(location)
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isFalse
    }

    @Test
    fun onLocationChangedShouldRemoveUpdateLocationWhenKeepTrackingIsNotRequired() {
        `when`(locationConfiguration.keepTracking()).thenReturn(false)
        googlePlayServicesLocationProvider.onLocationChanged(location)
        verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onLocationChangedShouldNotRemoveUpdateLocationWhenKeepTrackingIsRequired() {
        `when`(locationConfiguration.keepTracking()).thenReturn(true)
        googlePlayServicesLocationProvider.onLocationChanged(location)
        verify(mockedSource, never()).removeLocationUpdates()
    }

    @Test
    fun onLocationResultShouldCallOnLocationChangedWhenLocationListIsNotEmpty() {
        val locations: MutableList<Location> = ArrayList()
        locations.add(Location("1"))
        locations.add(Location("2"))
        val locationResult = LocationResult.create(locations)
        googlePlayServicesLocationProvider.onLocationResult(locationResult)
        verify(googlePlayServicesLocationProvider, atLeastOnce()).onLocationChanged(
            safeAny(
                Location::class.java
            )
        )
        verify(locationListener, atLeastOnce()).onLocationChanged(
            safeAny(
                Location::class.java
            )
        )
    }

    @Test
    fun onLocationResultShouldNotCallOnLocationChangedWhenLocationListIsEmpty() {
        val locations: List<Location> = ArrayList()
        val locationResult = LocationResult.create(locations)
        googlePlayServicesLocationProvider.onLocationResult(locationResult)
        verify(googlePlayServicesLocationProvider, never()).onLocationChanged(
            safeAny(
                Location::class.java
            )
        )
    }

    @Test
    fun onLocationResultShouldNotCallOnLocationChangedWhenLocationResultIsNull() {
        googlePlayServicesLocationProvider.onLocationResult(null)
        verify(googlePlayServicesLocationProvider, never()).onLocationChanged(
            safeAny(
                Location::class.java
            )
        )
    }

    @Test
    fun onResultShouldCallRequestLocationUpdateWhenSuccess() {
        googlePlayServicesLocationProvider.onSuccess(
            getSettingsResultWithSuccess(
                LocationSettingsStatusCodes.SUCCESS
            )
        )
        verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        googlePlayServicesLocationProvider
            .onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE))
        verify(googlePlayServicesLocationProvider)
            .settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
    }

    @Test
    fun onResultShouldCallResolveSettingsApiWhenResolutionRequired() {
        val settingsResultWith =
            getSettingsResultWithError(LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
        googlePlayServicesLocationProvider.onFailure(settingsResultWith)
        verify(googlePlayServicesLocationProvider).resolveSettingsApi(
            safeAny(
                ResolvableApiException::class.java
            )
        )
    }

    @Test
    fun onResultShouldCallSettingsApiFailWithSettingsDeniedWhenOtherCase() {
        val settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.CANCELED)
        googlePlayServicesLocationProvider.onFailure(settingsResultWith)
        verify(googlePlayServicesLocationProvider)
            .settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
    }

    @Test
    fun resolveSettingsApiShouldCallSettingsApiFailWhenThereIsNoActivity() {
        `when`(contextProcessor.activity).thenReturn(null)
        googlePlayServicesLocationProvider.resolveSettingsApi(ResolvableApiException(Status(1)))
        verify(googlePlayServicesLocationProvider)
            .settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE)
    }

    @Test
    @Throws(Exception::class)
    fun resolveSettingsApiShouldStartSettingsApiResolutionForResult() {
        val status = Status(1)
        val resolvable = ResolvableApiException(status)
        googlePlayServicesLocationProvider.resolveSettingsApi(resolvable)
        verify(mockedSource).startSettingsApiResolutionForResult(
            resolvable,
            (activity)
        )
    }

    @Test
    @Throws(Exception::class)
    fun resolveSettingsApiShouldCallSettingsApiFailWhenExceptionThrown() {
        val status = Status(1)
        val resolvable = ResolvableApiException(status)
        doThrow(SendIntentException()).`when`(mockedSource)
            .startSettingsApiResolutionForResult(
                resolvable,
                (activity)
            )


        googlePlayServicesLocationProvider.resolveSettingsApi(resolvable)
        verify(googlePlayServicesLocationProvider)
            .settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
    }

    @Test
    fun locationRequiredShouldCheckLocationSettingsWhenConfigurationAsksForSettingsApi() {
        `when`(googlePlayServicesConfiguration.askForSettingsApi()).thenReturn(true)
        googlePlayServicesLocationProvider.locationRequired()
        verify(mockedSource).checkLocationSettings()
    }

    @Test
    fun locationRequiredShouldRequestLocationUpdateWhenConfigurationDoesntRequireToAskForSettingsApi() {
        googlePlayServicesLocationProvider.locationRequired()
        verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun requestLocationUpdateShouldUpdateProcessTypeOnListener() {
        googlePlayServicesLocationProvider.requestLocationUpdate()
        verify(locationListener)
            .onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES)
    }

    @Test
    fun requestLocationUpdateShouldRequest() {
        googlePlayServicesLocationProvider.requestLocationUpdate()
        verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun settingsApiFailShouldCallFailWhenConfigurationFailOnSettingsApiSuspendedTrue() {
        `when`(googlePlayServicesConfiguration.failOnSettingsApiSuspended())
            .thenReturn(true)
        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN)
        verify(googlePlayServicesLocationProvider).failed(FailType.UNKNOWN)
    }

    @Test
    fun settingsApiFailShouldCallRequestLocationUpdateWhenConfigurationFailOnSettingsApiSuspendedFalse() {
        `when`(googlePlayServicesConfiguration.failOnSettingsApiSuspended())
            .thenReturn(false)
        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN)
        verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun failedShouldRedirectToListenerWhenFallbackToDefaultIsFalse() {
        `when`(googlePlayServicesConfiguration.fallbackToDefault()).thenReturn(false)
        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN)
        verify(locationListener).onLocationFailed(FailType.UNKNOWN)
    }

    @Test
    fun failedShouldCallFallbackWhenFallbackToDefaultIsTrue() {
        `when`(googlePlayServicesConfiguration.fallbackToDefault()).thenReturn(true)
        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN)
        verify(fallbackListener).onFallback()
    }

    @Test
    fun failedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider.isWaiting = true
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isTrue
        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN)
        Assertions.assertThat(googlePlayServicesLocationProvider.isWaiting).isFalse
    }

    private fun makeSettingsDialogIsOnTrue() {
        googlePlayServicesLocationProvider.onFailure(
            getSettingsResultWithError(
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED
            )
        )
    }

    companion object {
        private fun getSettingsResultWithSuccess(statusCode: Int): LocationSettingsResponse {
            val status = Status(statusCode, null, null)
            val result = LocationSettingsResponse()
            result.setResult(LocationSettingsResult(status, null))
            return result
        }

        private fun getSettingsResultWithError(statusCode: Int): Exception {
            val status = Status(statusCode, null, null)
            return if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                ResolvableApiException(status)
            } else {
                ApiException(status)
            }
        }
    }
}