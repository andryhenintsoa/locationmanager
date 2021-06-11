package mg.henkinn.locationmanager.provider.permissionprovider

import android.app.Activity
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import mg.henkinn.locationmanager.constants.RequestCode
import mg.henkinn.locationmanager.fakes.MockDialogProvider
import mg.henkinn.locationmanager.listener.PermissionListener
import mg.henkinn.locationmanager.safeEq
import mg.henkinn.locationmanager.view.ContextProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DefaultPermissionProviderTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Mock
    lateinit var fragment: Fragment

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var permissionListener: PermissionListener

    @Mock
    lateinit var permissionCompatSource: PermissionCompatSource
    private lateinit var defaultPermissionProvider: DefaultPermissionProvider
    private lateinit var mockDialogProvider: MockDialogProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockDialogProvider = MockDialogProvider("")
        defaultPermissionProvider =
            DefaultPermissionProvider(REQUIRED_PERMISSIONS, mockDialogProvider)
        defaultPermissionProvider.setContextProcessor(contextProcessor)
        defaultPermissionProvider.permissionListener = permissionListener
        defaultPermissionProvider.permissionCompatSource = permissionCompatSource
    }

    @Test
    fun executePermissionsRequestShouldNotifyDeniedWhenThereIsNoActivityOrFragment() {
        defaultPermissionProvider.executePermissionsRequest()
        verify(permissionListener).onPermissionsDenied()
    }

    @Test
    fun executePermissionsRequestShouldCallRequestPermissionsOnFragmentFirst() {
        `when`(contextProcessor.fragment).thenReturn(fragment)
        defaultPermissionProvider.executePermissionsRequest()
        verify(permissionCompatSource)
            .requestPermissions(
                safeEq(fragment),
                safeEq(REQUIRED_PERMISSIONS),
                safeEq(RequestCode.RUNTIME_PERMISSION)
            )
    }

    @Test
    fun executePermissionsRequestShouldCallRequestPermissionsOnActivityIfThereIsNoFragment() {
        `when`(contextProcessor.activity).thenReturn(activity)
        defaultPermissionProvider.executePermissionsRequest()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun checkRationaleForPermissionShouldReturnFalseIfThereIsNoActivityOrFragment() {
        assertThat(defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION)).isFalse
    }

    @Test
    fun checkRationaleForPermissionShouldCheckOnFragmentFirst() {
        `when`(contextProcessor.fragment).thenReturn(fragment)
        defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION)
        verify(permissionCompatSource).shouldShowRequestPermissionRationale(
            safeEq(fragment), safeEq(
                SINGLE_PERMISSION
            )
        )
    }

    @Test
    fun checkRationaleForPermissionShouldCheckOnActivityIfThereIsNoFragment() {
        `when`(contextProcessor.activity).thenReturn(activity)
        defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION)
        verify(permissionCompatSource).shouldShowRequestPermissionRationale(
            safeEq(activity), safeEq(
                SINGLE_PERMISSION
            )
        )
    }

    @Test
    fun shouldShowRequestPermissionRationaleShouldReturnTrueWhenAnyIsTrue() {
        `when`(contextProcessor.activity).thenReturn(activity)
        `when`(
            permissionCompatSource.shouldShowRequestPermissionRationale(
                safeEq(activity), safeEq(
                    REQUIRED_PERMISSIONS[0]
                )
            )
        )
            .thenReturn(true)
        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isTrue
    }

    @Test
    fun shouldShowRequestPermissionRationaleShouldReturnFalseWhenThereIsNoActivity() {
        `when`(contextProcessor.activity).thenReturn(null)
        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isFalse
    }

    @Test
    fun shouldShowRequestPermissionRationaleShouldReturnFalseWhenThereIsNoDialogProvider() {
        defaultPermissionProvider = DefaultPermissionProvider(REQUIRED_PERMISSIONS, null)
        defaultPermissionProvider.setContextProcessor(contextProcessor)
        defaultPermissionProvider.permissionListener = permissionListener
        defaultPermissionProvider.permissionCompatSource = permissionCompatSource
        makeShouldShowRequestPermissionRationaleTrue()
        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isFalse
    }

    @Test
    fun requestPermissionsShouldReturnFalseWhenThereIsNoActivity() {
        assertThat(defaultPermissionProvider.requestPermissions()).isFalse
    }

    @Test
    fun requestPermissionsShouldRequestWhenShouldShowRequestPermissionRationaleIsFalse() {
        `when`(contextProcessor.activity).thenReturn(activity)
        defaultPermissionProvider.requestPermissions()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun requestPermissionsShouldShowRationaleIfRequired() {
        makeShouldShowRequestPermissionRationaleTrue()
        defaultPermissionProvider.requestPermissions()
        verify(mockDialogProvider.getDialog(activity))?.show()
    }

    @Test
    fun onPositiveButtonClickShouldRequestPermission() {
        `when`(contextProcessor.activity).thenReturn(activity)
        defaultPermissionProvider.onPositiveButtonClick()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun onNegativeButtonClickShouldNotifyPermissionDenied() {
        defaultPermissionProvider.onNegativeButtonClick()
        verify(permissionListener).onPermissionsDenied()
    }

    @Test
    fun onRequestPermissionsResultShouldDoNothingWhenRequestCodeIsNotMatched() {
        defaultPermissionProvider.onRequestPermissionsResult(
            RequestCode.GOOGLE_PLAY_SERVICES,
            arrayOf(),
            intArrayOf(1)
        )
        verifyZeroInteractions(permissionListener)
    }

    @Test
    fun onRequestPermissionsResultShouldNotifyDeniedIfAny() {
        defaultPermissionProvider.onRequestPermissionsResult(
            RequestCode.RUNTIME_PERMISSION,
            REQUIRED_PERMISSIONS, intArrayOf(GRANTED, GRANTED, DENIED)
        )
        verify(permissionListener).onPermissionsDenied()
    }

    @Test
    fun onRequestPermissionsResultShouldNotifyGrantedIfAll() {
        defaultPermissionProvider.onRequestPermissionsResult(
            RequestCode.RUNTIME_PERMISSION,
            REQUIRED_PERMISSIONS, intArrayOf(GRANTED, GRANTED, GRANTED)
        )
        verify(permissionListener).onPermissionsGranted()
    }

    private fun makeShouldShowRequestPermissionRationaleTrue() {
        `when`(contextProcessor.activity).thenReturn(activity)
        `when`(
            permissionCompatSource.shouldShowRequestPermissionRationale(
                safeEq(activity), safeEq(
                    REQUIRED_PERMISSIONS[0]
                )
            )
        )
            .thenReturn(true)
    }

    private fun verifyRequestPermissionOnActivity() {
        verify(permissionCompatSource)
            .requestPermissions(
                safeEq(activity),
                safeEq(REQUIRED_PERMISSIONS),
                safeEq(RequestCode.RUNTIME_PERMISSION)
            )
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            "really_important_permission",
            "even_more_important", "super_important_one"
        )
        private val SINGLE_PERMISSION = REQUIRED_PERMISSIONS[0]
        private const val GRANTED = PackageManager.PERMISSION_GRANTED
        private const val DENIED = PackageManager.PERMISSION_DENIED
    }
}