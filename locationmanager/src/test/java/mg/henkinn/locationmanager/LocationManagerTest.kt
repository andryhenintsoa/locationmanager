package mg.henkinn.locationmanager

import android.content.Intent
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.configuration.PermissionConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.ProcessType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.listener.LocationListener
import mg.henkinn.locationmanager.provider.locationprovider.DispatcherLocationProvider
import mg.henkinn.locationmanager.provider.locationprovider.LocationProvider
import mg.henkinn.locationmanager.provider.permissionprovider.PermissionProvider
import mg.henkinn.locationmanager.view.ContextProcessor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class LocationManagerTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var locationProvider: LocationProvider

    @Mock
    lateinit var permissionProvider: PermissionProvider

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var permissionConfiguration: PermissionConfiguration

    @Before
    @Throws(Exception::class)
    fun setUp() {
        `when`(locationConfiguration.permissionConfiguration()).thenReturn(
            permissionConfiguration
        )
        `when`(locationConfiguration.permissionConfiguration().permissionProvider()).thenReturn(
            permissionProvider
        )
    }

    // region Build Tests
    @Test
    fun buildingWithoutConfigurationShouldThrowException() {
        expectedException.expect(IllegalStateException::class.java)
        LocationManager.Builder(contextProcessor)
            .locationProvider(locationProvider)
            .notify(locationListener)
            .build()
    }

    @Test
    fun buildingWithoutProviderShouldUseDispatcherLocationProvider() {
        val locationManager: LocationManager = LocationManager.Builder(contextProcessor)
            .configuration(locationConfiguration)
            .notify(locationListener)
            .build()

        val provider = locationManager.activeProvider()
        assertNotNull(provider)
        assert(provider is DispatcherLocationProvider)
    }

    @Test
    fun buildingShouldCallConfigureAndSetListenerOnProvider() {
        buildLocationManager()
        verify(locationProvider)?.configure(
            contextProcessor,
            locationConfiguration,
            locationListener
        )
    }

    @Test
    fun buildingShouldSetContextProcessorAndListenerToPermissionListener() {
        val locationManager = buildLocationManager()
        verify(permissionProvider)?.setContextProcessor(contextProcessor)
        verify(permissionProvider)?.permissionListener = locationManager
    }
    // endregion

    // endregion
    // region Redirect Tests
    @Test
    fun whenOnPauseShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onPause()
        verify(locationProvider)?.onPause()
    }

    @Test
    fun whenOnResumeShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onResume()
        verify(locationProvider)?.onResume()
    }

    @Test
    fun whenOnDestroyShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onDestroy()
        verify(locationProvider)?.onDestroy()
    }

    @Test
    fun whenCancelShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.cancel()
        verify(locationProvider)?.cancel()
    }

    @Test
    fun whenOnActivityResultShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        val requestCode = RequestCode.GPS_ENABLE
        val resultCode = 2
        val data = Intent()
        locationManager.onActivityResult(requestCode, resultCode, data)
        verify(locationProvider)?.onActivityResult(requestCode, resultCode, data)
    }

    @Test
    fun whenGetShouldRedirectToLocationProviderWhenPermissionIsGranted() {
        `when`(permissionProvider.hasPermission()).thenReturn(true)
        val locationManager = buildLocationManager()
        locationManager.get()
        verify(locationProvider)?.get()
    }
    // endregion

    // endregion
    // region Retrieve Tests
    @Test
    fun isWaitingForLocationShouldRetrieveFromLocationProvider() {
        `when`(locationProvider.isWaiting).thenReturn(true)
        val locationManager = buildLocationManager()
        assert(locationManager.isWaitingForLocation)
        verify(locationProvider)?.isWaiting
    }

    @Test
    fun isAnyDialogShowingShouldRetrieveFromLocationProvider() {
        `when`(locationProvider.isDialogShowing).thenReturn(true)
        val locationManager = buildLocationManager()
        assert(locationManager.isAnyDialogShowing)
        verify(locationProvider)?.isDialogShowing
    }
    // endregion

    // endregion
    @Test
    fun whenGetCalledShouldStartPermissionRequest() {
        val locationManager = buildLocationManager()
        locationManager.get()
        verify(permissionProvider)?.hasPermission()
        verify(permissionProvider)?.requestPermissions()
    }

    @Test
    fun whenRequestPermissionsAreAlreadyGrantedShouldNotifyListenerWithTrue() {
        `when`(permissionProvider.hasPermission()).thenReturn(true)
        val locationManager = buildLocationManager()
        locationManager.askForPermission()
        verify(locationListener)?.onPermissionGranted(eq(true))
    }

    @Test
    fun whenRequestedPermissionsAreGrantedShouldNotifyListenerWithFalse() {
        val locationManager = buildLocationManager()
        `when`(permissionProvider.permissionListener).thenReturn(locationManager)
        permissionProvider.permissionListener!!.onPermissionsGranted()
        verify(locationListener)?.onPermissionGranted(eq(false))
    }

    @Test
    fun whenRequestedPermissionsAreDeniedShouldCallFailOnListener() {
        val locationManager = buildLocationManager()
        `when`(permissionProvider.permissionListener).thenReturn(locationManager)
        permissionProvider.permissionListener!!.onPermissionsDenied()
        verify(locationListener)?.onLocationFailed(FailType.PERMISSION_DENIED)
    }

    @Test
    fun whenAskForPermissionShouldNotifyListenerWithProcessTypeChanged() {
        val locationManager = buildLocationManager()
        locationManager.askForPermission()
        verify(locationListener)?.onProcessTypeChanged(ProcessType.ASKING_PERMISSIONS)
    }

    @Test
    fun whenRequestingPermissionIsNotPossibleThenItShouldFail() {
        `when`(permissionProvider.requestPermissions()).thenReturn(false)
        val locationManager = buildLocationManager()
        locationManager.askForPermission()
        verify(locationListener)?.onLocationFailed(FailType.PERMISSION_DENIED)
    }

    private fun buildLocationManager(): LocationManager {
        return LocationManager.Builder(contextProcessor)
            .locationProvider(locationProvider)
            .configuration(locationConfiguration)
            .notify(locationListener)
            .build()
    }

}