package mg.henkinn.locationmanager.configuration

import android.Manifest
import mg.henkinn.locationmanager.fakes.MockDialogProvider
import mg.henkinn.locationmanager.provider.permissionprovider.DefaultPermissionProvider
import mg.henkinn.locationmanager.provider.dialogprovider.SimpleMessageDialogProvider
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class PermissionConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun checkDefaultValues() {
        val configuration = PermissionConfiguration.Builder().build()
        assertThat(configuration.permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
        assertThat(configuration.permissionProvider()?.dialogProvider).isNull()
        assertThat(configuration.permissionProvider()?.requiredPermissions)
            .isNotEmpty
            .isEqualTo(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
    }

    @Test
    fun requiredPermissionsShouldThrowExceptionWhenSetToNull() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredPermissions"))
        PermissionConfiguration.Builder().requiredPermissions(null)
    }

    @Test
    fun requiredPermissionsShouldThrowExceptionWhenSetEmpty() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredPermissions"))
        PermissionConfiguration.Builder().requiredPermissions(arrayOf())
    }

    @Test
    fun requiredPermissionsShouldSetPermissionsWhenSetNotEmpty() {
        val permissionConfiguration = PermissionConfiguration.Builder().requiredPermissions(
            Defaults.LOCATION_PERMISSIONS
        ).build()
        assertThat(
            permissionConfiguration.permissionProvider()?.requiredPermissions
        ).containsAll(
            Defaults.LOCATION_PERMISSIONS.asList()
        )
    }

    @Test
    fun whenRationaleMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        val rationaleMessage = "some_text"
        val configuration = PermissionConfiguration.Builder()
            .rationaleMessage(rationaleMessage)
            .build()
        assertThat(configuration.permissionProvider()?.dialogProvider)
            .isNotNull
            .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        assertThat(
            (configuration.permissionProvider()
                ?.dialogProvider as SimpleMessageDialogProvider).message()
        )
            .isEqualTo(rationaleMessage)
    }

    @Test
    fun whenDialogProviderIsSetMessageShouldBeIgnored() {
        val rationaleMessage = "some_text"
        val configuration = PermissionConfiguration.Builder()
            .rationaleDialogProvider(MockDialogProvider(rationaleMessage))
            .rationaleMessage("ignored_text")
            .build()
        assertThat(configuration.permissionProvider()?.dialogProvider)
            .isNotNull
            .isExactlyInstanceOf(MockDialogProvider::class.java)
        assertThat(
            (configuration.permissionProvider()?.dialogProvider as MockDialogProvider).message()
        )
            .isEqualTo(rationaleMessage)
    }
}