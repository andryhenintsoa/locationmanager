package mg.henkinn.locationmanager.provider.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.content.Intent
import com.google.android.gms.common.ConnectionResult
import mg.henkinn.locationmanager.configuration.DefaultProviderConfiguration
import mg.henkinn.locationmanager.configuration.GooglePlayServicesConfiguration
import mg.henkinn.locationmanager.configuration.LocationConfiguration
import mg.henkinn.locationmanager.constants.FailType
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.helper.continuoustask.ContinuousTask
import mg.henkinn.locationmanager.listener.LocationListener
import mg.henkinn.locationmanager.safeAny
import mg.henkinn.locationmanager.safeEq
import mg.henkinn.locationmanager.view.ContextProcessor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.assertj.core.api.Assertions.assertThat

@RunWith(MockitoJUnitRunner::class)
class DispatcherLocationProviderTest {
    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var dialog: Dialog

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var googlePlayServicesConfiguration: GooglePlayServicesConfiguration

    @Mock
    lateinit var defaultProviderConfiguration: DefaultProviderConfiguration

    @Mock
    internal lateinit var dispatcherLocationSource: DispatcherLocationSource

    @Mock
    lateinit var defaultLocationProvider: DefaultLocationProvider

    @Mock
    lateinit var googlePlayServicesLocationProvider: GooglePlayServicesLocationProvider

    @Mock
    lateinit var continuousTask: ContinuousTask
    private lateinit var dispatcherLocationProvider: DispatcherLocationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        dispatcherLocationProvider = spy(DispatcherLocationProvider())
        dispatcherLocationProvider.configure(
            contextProcessor,
            locationConfiguration,
            locationListener
        )
        dispatcherLocationProvider.setDispatcherLocationSource(dispatcherLocationSource)
        `when`(locationConfiguration.defaultProviderConfiguration()).thenReturn(
            defaultProviderConfiguration
        )
        `when`(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(
            googlePlayServicesConfiguration
        )
        `when`(googlePlayServicesConfiguration.googlePlayServicesWaitPeriod()).thenReturn(
            GOOGLE_PLAY_SERVICES_SWITCH_PERIOD
        )
        `when`(dispatcherLocationSource.createDefaultLocationProvider()).thenReturn(
            defaultLocationProvider
        )
        `when`(
            dispatcherLocationSource.createGooglePlayServicesLocationProvider(
                dispatcherLocationProvider
            )
        )
            .thenReturn(googlePlayServicesLocationProvider)
        `when`(dispatcherLocationSource.gpServicesSwitchTask()).thenReturn(continuousTask)
        `when`(contextProcessor.context).thenReturn(context)
        `when`(contextProcessor.activity).thenReturn(activity)
    }

    @Test
    fun onPauseShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        dispatcherLocationProvider.onPause()
        verify(defaultLocationProvider).onPause()
        verify(continuousTask).pause()
    }

    @Test
    fun onResumeShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        dispatcherLocationProvider.onResume()
        verify(defaultLocationProvider).onResume()
        verify(continuousTask).resume()
    }

    @Test
    fun onDestroyShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        dispatcherLocationProvider.onDestroy()
        verify(defaultLocationProvider).onDestroy()
        verify(continuousTask).stop()
    }

    @Test
    fun cancelShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        dispatcherLocationProvider.cancel()
        verify(defaultLocationProvider).cancel()
        verify(continuousTask).stop()
    }

    @Test
    fun isWaitingShouldReturnFalseWhenNoActiveProvider() {
        assertThat(dispatcherLocationProvider.isWaiting).isFalse
    }

    @Test
    fun isWaitingShouldRetrieveFromActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        `when`(defaultLocationProvider.isWaiting).thenReturn(true)
        assertThat(dispatcherLocationProvider.isWaiting).isTrue
        verify(defaultLocationProvider).isWaiting
    }

    @Test
    fun isDialogShowingShouldReturnFalseWhenNoDialogShown() {
        assertThat(dispatcherLocationProvider.isDialogShowing).isFalse
    }

    // so dialog is not null
    @Test
    fun isDialogShowingShouldReturnTrueWhenGpServicesIsShowing() {
        showGpServicesDialogShown() // so dialog is not null
        `when`(dialog.isShowing).thenReturn(true)
        assertThat(dispatcherLocationProvider.isDialogShowing).isTrue
    }

    // so provider is not null
    @Test
    fun isDialogShowingShouldRetrieveFromActiveProviderWhenExists() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider) // so provider is not null
        `when`(defaultLocationProvider.isDialogShowing).thenReturn(true)
        assertThat(dispatcherLocationProvider.isDialogShowing).isTrue
        verify(defaultLocationProvider).isDialogShowing
    }

    @Test
    fun runScheduledTaskShouldDoNothingWhenActiveProviderIsNotGPServices() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        verify(defaultLocationProvider).configure(dispatcherLocationProvider)
        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        verifyNoMoreInteractions(defaultLocationProvider)
    }

    @Test
    fun runScheduledTaskShouldDoNothingWhenNoOnGoingTask() {
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider)
        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider)
        `when`(googlePlayServicesLocationProvider.isWaiting).thenReturn(false)
        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        verify(googlePlayServicesLocationProvider).isWaiting
        verifyNoMoreInteractions(googlePlayServicesLocationProvider)
    }

    @Test
    fun runScheduledTaskShouldCancelCurrentProviderAndRunWithDefaultWhenGpServicesTookEnough() {
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider)
        `when`(googlePlayServicesLocationProvider.isWaiting).thenReturn(true)
        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        verify(dispatcherLocationProvider)?.cancel()
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun onActivityResultShouldRedirectToActiveProvider() {
        val data = Intent()
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)

        dispatcherLocationProvider.onActivityResult(RequestCode.RUNTIME_PERMISSION, -1, data)
        verify(defaultLocationProvider).onActivityResult(
            safeEq(RequestCode.RUNTIME_PERMISSION),
            safeEq(-1),
            safeEq(data)
        )
    }

    @Test
    fun onActivityResultShouldCallCheckGooglePlayServicesAvailabilityWithFalseWhenRequestCodeMatches() {
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider.onActivityResult(RequestCode.GOOGLE_PLAY_SERVICES, -1, null)
        verify(dispatcherLocationProvider)?.checkGooglePlayServicesAvailability(safeEq(false))
    }

    @Test
    fun shouldContinueWithDefaultProviderIfThereIsNoGpServicesConfiguration() {
        `when`(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(null)
        dispatcherLocationProvider.get()
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun shouldCallCheckGooglePlayServicesAvailabilityWithTrue() {
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(
            RESOLVABLE_ERROR
        )
        `when`(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true)
        dispatcherLocationProvider.get()
        verify(dispatcherLocationProvider)?.checkGooglePlayServicesAvailability(
            safeEq(
                true
            )
        )
    }

    @Test
    fun onFallbackShouldCallCancelAndContinueWithDefaultProviders() {
        dispatcherLocationProvider.onFallback()
        verify(dispatcherLocationProvider)?.cancel()
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldGetLocationWhenApiIsAvailable() {
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(ConnectionResult.SUCCESS)
        dispatcherLocationProvider.checkGooglePlayServicesAvailability(false) // could be also true, wouldn't matter
        verify(dispatcherLocationProvider)?.getLocationFromGooglePlayServices()
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldContinueWithDefaultWhenCalledWithFalse() {
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider.checkGooglePlayServicesAvailability(false)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldAskForGooglePlayServicesWhenCalledWithTrue() {
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider.checkGooglePlayServicesAvailability(true)
        verify(dispatcherLocationProvider)?.askForGooglePlayServices(safeEq(RESOLVABLE_ERROR))
    }

    @Test
    fun askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenErrorNotResolvable() {
        `when`(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true)
        `when`(dispatcherLocationSource.isGoogleApiErrorUserResolvable(NOT_RESOLVABLE_ERROR)).thenReturn(
            false
        )
        dispatcherLocationProvider.askForGooglePlayServices(NOT_RESOLVABLE_ERROR)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenConfigurationNoRequire() {
        `when`(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(false)
        dispatcherLocationProvider.askForGooglePlayServices(RESOLVABLE_ERROR)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun askForGooglePlayServicesShouldResolveGooglePlayServicesWhenPossible() {
        `when`(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true)
        `when`(dispatcherLocationSource.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(
            true
        )
        dispatcherLocationProvider.askForGooglePlayServices(RESOLVABLE_ERROR)
        verify(dispatcherLocationProvider)?.resolveGooglePlayServices(RESOLVABLE_ERROR)
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenResolveDialogIsNull() {
        `when`(
            dispatcherLocationSource.getGoogleApiErrorDialog(
                safeEq(activity), safeEq(RESOLVABLE_ERROR),
                safeEq(RequestCode.GOOGLE_PLAY_SERVICES), safeAny(OnCancelListener::class.java)
            )
        ).thenReturn(null)
        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenErrorCannotBeResolved() {
        val unresolvableError = ConnectionResult.SERVICE_INVALID
        val dismissListener = arrayOfNulls<DialogInterface.OnDismissListener>(1)
        `when`(
            dispatcherLocationSource.getGoogleApiErrorDialog(
                safeEq(activity), safeEq(unresolvableError),
                safeEq(RequestCode.GOOGLE_PLAY_SERVICES), safeAny(OnCancelListener::class.java)
            )
        ).thenReturn(dialog)

        // catch and store real OnDismissListener listener
        doAnswer { invocation ->
            dismissListener[0] = invocation.getArgument(0)
            null
        }.`when`(dialog)
            .setOnDismissListener(safeAny(DialogInterface.OnDismissListener::class.java))

        // simulate dialog dismiss event
        doAnswer {
            dismissListener[0]!!.onDismiss(dialog)
            null
        }.`when`(dialog).dismiss()
        dispatcherLocationProvider.resolveGooglePlayServices(unresolvableError)
        verify(dialog).show()
        dialog.dismiss() // Simulate dismiss dialog (error cannot be resolved)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenWhenResolveDialogIsCancelled() {
        val cancelListener: Array<OnCancelListener?> = arrayOfNulls(1)

        // catch and store real OnCancelListener listener
        doAnswer { invocation ->
            cancelListener[0] = invocation.getArgument(3)
            dialog
        }.`when`(dispatcherLocationSource).getGoogleApiErrorDialog(
            safeEq(activity), safeEq(RESOLVABLE_ERROR),
            safeEq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener::class.java)
        )

        // simulate dialog cancel event
        doAnswer {
            cancelListener[0]!!.onCancel(dialog)
            null
        }.`when`(dialog).cancel()
        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR)
        verify(dialog).show()
        dialog.cancel() // Simulate cancel dialog (user cancelled dialog)
        verify(dispatcherLocationProvider)?.continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldShowDialogWhenResolveDialogNotNull() {
        `when`(
            dispatcherLocationSource.getGoogleApiErrorDialog(
                safeEq(activity), safeEq(RESOLVABLE_ERROR),
                safeEq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener::class.java)
            )
        ).thenReturn(dialog)
        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR)
        verify(dialog).show()
    }

    @Test
    fun locationFromGooglePlayServices() {
        dispatcherLocationProvider.getLocationFromGooglePlayServices()
        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider)
        verify(continuousTask).delayed(GOOGLE_PLAY_SERVICES_SWITCH_PERIOD)
        verify(googlePlayServicesLocationProvider).get()
    }

    @Test
    fun continueWithDefaultProvidersShouldNotifyFailWhenNoDefaultProviderConfiguration() {
        `when`(locationConfiguration.defaultProviderConfiguration()).thenReturn(null)
        dispatcherLocationProvider.continueWithDefaultProviders()
        verify(locationListener).onLocationFailed(safeEq(FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE))
    }

    @Test
    fun continueWithDefaultProviders() {
        dispatcherLocationProvider.continueWithDefaultProviders()
        verify(defaultLocationProvider).configure(dispatcherLocationProvider)
        verify(defaultLocationProvider).get()
    }

    @Test
    fun setLocationProviderShouldConfigureGivenProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider)
        verify(defaultLocationProvider).configure(dispatcherLocationProvider)
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider)
        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider)
    }

    private fun showGpServicesDialogShown() {
        `when`(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true)
        `when`(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        `when`(dispatcherLocationSource.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(
            true
        )
        `when`(
            dispatcherLocationSource.getGoogleApiErrorDialog(
                safeEq(activity), safeEq(RESOLVABLE_ERROR),
                safeEq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener::class.java)
            )
        ).thenReturn(dialog)
        dispatcherLocationProvider.checkGooglePlayServicesAvailability(true)
        verify(dialog).show()
    }

    companion object {
        private const val GOOGLE_PLAY_SERVICES_SWITCH_PERIOD = (5 * 1000).toLong()
        private const val RESOLVABLE_ERROR = ConnectionResult.SERVICE_MISSING
        private const val NOT_RESOLVABLE_ERROR = ConnectionResult.INTERNAL_ERROR
    }
}