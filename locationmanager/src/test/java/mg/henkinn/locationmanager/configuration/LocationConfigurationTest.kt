package mg.henkinn.locationmanager.configuration

import mg.henkinn.locationmanager.provider.permissionprovider.DefaultPermissionProvider
import mg.henkinn.locationmanager.provider.permissionprovider.StubPermissionProvider
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class LocationConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()
    @Test
    fun whenNoProviderConfigurationIsSetBuildShouldThrowException() {
        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(
            CoreMatchers.containsString("GooglePlayServicesConfiguration and DefaultProviderConfiguration")
        )
        LocationConfiguration.Builder().build()
    }

    @Test
    fun checkDefaultValues() {
        val configuration = configuration
        assertThat(configuration.keepTracking()).isFalse
    }

    @Test
    fun whenNoPermissionConfigurationIsSetDefaultConfigurationShouldContainStubProvider() {
        val configuration = configuration
        assertThat(configuration.permissionConfiguration()).isNotNull
        assertThat(configuration.permissionConfiguration().permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(StubPermissionProvider::class.java)
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration = configuration
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        assertThat(firstClone.keepTracking())
            .isEqualTo(secondClone.keepTracking())
            .isFalse
        assertThat(firstClone.permissionConfiguration())
            .isEqualTo(secondClone.permissionConfiguration())
            .isNotNull
        assertThat(firstClone.defaultProviderConfiguration())
            .isEqualTo(secondClone.defaultProviderConfiguration())
            .isNotNull
        assertThat(firstClone.googlePlayServicesConfiguration())
            .isEqualTo(secondClone.googlePlayServicesConfiguration())
            .isNotNull
    }

    @Test
    fun clonedConfigurationIsIndependent() {
        val configuration = configuration
        val clone = configuration.newBuilder()
            .askForPermission(PermissionConfiguration.Builder().build())
            .build()
        assertThat(configuration.permissionConfiguration())
            .isNotEqualTo(clone.permissionConfiguration())
        assertThat(configuration.permissionConfiguration().permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(StubPermissionProvider::class.java)
        assertThat(clone.permissionConfiguration().permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
    }

    private val configuration: LocationConfiguration
        get() = LocationConfiguration.Builder()
            .useDefaultProviders(DefaultProviderConfiguration.Builder().build())
            .useGooglePlayServices(GooglePlayServicesConfiguration.Builder().build())
            .build()
}