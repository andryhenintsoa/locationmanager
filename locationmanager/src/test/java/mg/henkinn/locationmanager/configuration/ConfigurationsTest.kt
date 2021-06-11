package mg.henkinn.locationmanager.configuration

import mg.henkinn.locationmanager.configuration.Configurations.defaultConfiguration
import mg.henkinn.locationmanager.configuration.Configurations.silentConfiguration
import mg.henkinn.locationmanager.provider.dialogprovider.SimpleMessageDialogProvider
import mg.henkinn.locationmanager.provider.permissionprovider.DefaultPermissionProvider
import mg.henkinn.locationmanager.provider.permissionprovider.StubPermissionProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigurationsTest {
    @Test
    fun silentConfigurationWithoutParameterShouldKeepTracking() {
        assertThat(silentConfiguration().keepTracking()).isTrue
    }

    @Test
    fun silentConfigurationCheckDefaultValues() {
        val silentConfiguration = silentConfiguration(false)
        assertThat(silentConfiguration.keepTracking()).isFalse
        assertThat(silentConfiguration.permissionConfiguration()).isNotNull
        assertThat(silentConfiguration.permissionConfiguration().permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(StubPermissionProvider::class.java)
        assertThat(silentConfiguration.googlePlayServicesConfiguration()).isNotNull
        assertThat(
            silentConfiguration.googlePlayServicesConfiguration()!!.askForSettingsApi()
        ).isFalse
        assertThat(silentConfiguration.defaultProviderConfiguration()).isNotNull
    }

    @Test
    fun defaultConfigurationCheckDefaultValues() {
        val defaultConfiguration = defaultConfiguration("rationale", "gps")
        assertThat(defaultConfiguration.keepTracking()).isFalse
        assertThat(defaultConfiguration.permissionConfiguration()).isNotNull
        assertThat(defaultConfiguration.permissionConfiguration().permissionProvider())
            .isNotNull
            .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
        assertThat(
            defaultConfiguration.permissionConfiguration().permissionProvider()?.dialogProvider
        )
            .isNotNull
            .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        assertThat(
            (defaultConfiguration.permissionConfiguration()
                .permissionProvider()?.dialogProvider as SimpleMessageDialogProvider).message()
        ).isEqualTo("rationale")
        assertThat(defaultConfiguration.googlePlayServicesConfiguration()).isNotNull
        assertThat(defaultConfiguration.defaultProviderConfiguration()).isNotNull
        assertThat(
            defaultConfiguration.defaultProviderConfiguration()!!.askForEnableGPS()
        ).isTrue
        assertThat(
            defaultConfiguration.defaultProviderConfiguration()!!.gpsDialogProvider()
        )
            .isNotNull
            .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        assertThat(
            (defaultConfiguration.defaultProviderConfiguration()!!
                .gpsDialogProvider() as SimpleMessageDialogProvider?)!!.message()
        ).isEqualTo("gps")
    }
}