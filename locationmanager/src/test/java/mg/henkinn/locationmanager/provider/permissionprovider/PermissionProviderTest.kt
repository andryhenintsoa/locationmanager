package mg.henkinn.locationmanager.provider.permissionprovider

import android.content.Context
import mg.henkinn.locationmanager.fakes.FakePermissionProvider
import mg.henkinn.locationmanager.view.ContextProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionProviderTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var context: Context
    private lateinit var permissionProvider: FakePermissionProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        permissionProvider = FakePermissionProvider(REQUIRED_PERMISSIONS, null)
        permissionProvider.setContextProcessor(contextProcessor)
    }

    @Test
    fun creatingInstanceWithNoRequiredPermissionShouldThrowException() {
        expectedException.expect(IllegalStateException::class.java)
        permissionProvider = FakePermissionProvider(arrayOf(), null)
    }

    @Test
    fun whenThereIsNoContextHasPermissionShouldReturnFalse() {
        assertThat(permissionProvider.hasPermission()).isFalse
    }

    @Test
    fun whenThereIsContextHasPermissionShouldReturnTrue() {
        `when`(contextProcessor.context).thenReturn(context)
        permissionProvider.grantPermission(true)
        assertThat(permissionProvider.hasPermission()).isTrue
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf("really_important_permission")
    }
}