package mg.henkinn.locationmanager.configuration

import mg.henkinn.locationmanager.constants.ProviderType
import mg.henkinn.locationmanager.fakes.MockDialogProvider
import mg.henkinn.locationmanager.provider.dialogprovider.SimpleMessageDialogProvider
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class DefaultProviderConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()
    @Test
    fun checkDefaultValues() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        assertThat(configuration.requiredTimeInterval()).isEqualTo((5 * MINUTE).toLong())
        assertThat(configuration.requiredDistanceInterval()).isEqualTo(0)
        assertThat(configuration.acceptableAccuracy()).isEqualTo(5.0f)
        assertThat(configuration.acceptableTimePeriod()).isEqualTo((5 * MINUTE).toLong())
        assertThat(configuration.gpsWaitPeriod()).isEqualTo((20 * SECOND).toLong())
        assertThat(configuration.networkWaitPeriod()).isEqualTo((20 * SECOND).toLong())
        assertThat(configuration.gpsDialogProvider()).isNull()
    }

    @Test
    fun requiredTimeIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredTimeInterval"))
        DefaultProviderConfiguration.Builder().requiredTimeInterval(-1)
    }

    @Test
    fun requiredDistanceIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredDistanceInterval"))
        DefaultProviderConfiguration.Builder().requiredDistanceInterval(-1)
    }

    @Test
    fun acceptableAccuracyShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableAccuracy"))
        DefaultProviderConfiguration.Builder().acceptableAccuracy(-1f)
    }

    @Test
    fun acceptableTimePeriodShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableTimePeriod"))
        DefaultProviderConfiguration.Builder().acceptableTimePeriod(-1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenNetworkWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.NETWORK, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGPSWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GPS, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenDefaultProvidersWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("GooglePlayServices"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 1)
    }

    @Test
    fun setWaitPeriodShouldSetPeriodsWhenDefaultProvidersIsSet() {
        val providerConfiguration =
            DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, 1)
                .build()
        assertThat(providerConfiguration.gpsWaitPeriod()).isEqualTo(1)
        assertThat(providerConfiguration.networkWaitPeriod()).isEqualTo(1)
    }

    @Test
    fun whenGpsMessageAndDialogProviderNotSetAskForGPSEnableShouldReturnFalse() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        assertThat(configuration.askForEnableGPS()).isFalse
    }

    @Test
    fun whenGpsMessageSetAskForGPSEnableShouldReturnTrue() {
        val configuration = DefaultProviderConfiguration.Builder()
            .gpsMessage("some_text")
            .build()
        assertThat(configuration.askForEnableGPS()).isTrue
    }

    @Test
    fun whenDialogProviderSetAskForGPSEnableShouldReturnTrue() {
        val configuration = DefaultProviderConfiguration.Builder()
            .gpsDialogProvider(MockDialogProvider("some_text"))
            .build()
        assertThat(configuration.askForEnableGPS()).isTrue
    }

    @Test
    fun whenGpsMessageIsEmptyAndDialogProviderIsNotSetThenDialogProviderShouldBeNull() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        assertThat(configuration.gpsDialogProvider()).isNull()
    }

    @Test
    fun whenGpsMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        val gpsMessage = "some_text"
        val configuration = DefaultProviderConfiguration.Builder()
            .gpsMessage(gpsMessage)
            .build()
        assertThat(configuration.gpsDialogProvider())
            .isNotNull
            .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        assertThat((configuration.gpsDialogProvider() as SimpleMessageDialogProvider?)!!.message()).isEqualTo(
            gpsMessage
        )
    }

    @Test
    fun whenDialogProviderIsSetMessageShouldBeIgnored() {
        val gpsMessage = "some_text"
        val configuration = DefaultProviderConfiguration.Builder()
            .gpsMessage("ignored_message")
            .gpsDialogProvider(MockDialogProvider(gpsMessage))
            .build()
        assertThat(configuration.gpsDialogProvider())
            .isNotNull
            .isExactlyInstanceOf(MockDialogProvider::class.java)
        assertThat((configuration.gpsDialogProvider() as MockDialogProvider).message()).isEqualTo(
            gpsMessage
        )
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration = DefaultProviderConfiguration.Builder()
            .gpsDialogProvider(MockDialogProvider("some_text"))
            .build()
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        assertThat(firstClone.requiredTimeInterval())
            .isEqualTo(secondClone.requiredTimeInterval())
            .isEqualTo((5 * MINUTE).toLong())
        assertThat(firstClone.requiredDistanceInterval())
            .isEqualTo(secondClone.requiredDistanceInterval())
            .isEqualTo(0)
        assertThat(firstClone.acceptableAccuracy())
            .isEqualTo(secondClone.acceptableAccuracy())
            .isEqualTo(5.0f)
        assertThat(firstClone.acceptableTimePeriod())
            .isEqualTo(secondClone.acceptableTimePeriod())
            .isEqualTo((5 * MINUTE).toLong())
        assertThat(firstClone.gpsWaitPeriod())
            .isEqualTo(secondClone.gpsWaitPeriod())
            .isEqualTo((20 * SECOND).toLong())
        assertThat(firstClone.networkWaitPeriod())
            .isEqualTo(secondClone.networkWaitPeriod())
            .isEqualTo((20 * SECOND).toLong())
        assertThat(firstClone.gpsDialogProvider())
            .isEqualTo(secondClone.gpsDialogProvider())
            .isNotNull
            .isExactlyInstanceOf(MockDialogProvider::class.java)
    }

    companion object {
        private const val SECOND = 1000
        private const val MINUTE = 60 * SECOND
    }
}