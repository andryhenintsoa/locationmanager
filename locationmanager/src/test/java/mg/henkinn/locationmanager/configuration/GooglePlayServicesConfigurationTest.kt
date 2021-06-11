package mg.henkinn.locationmanager.configuration

import com.google.android.gms.location.LocationRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class GooglePlayServicesConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun checkDefaultValues() {
        val configuration = GooglePlayServicesConfiguration.Builder().build()
        assertThat(configuration.locationRequest())
            .isEqualTo(createDefaultLocationRequest())
        assertThat(configuration.fallbackToDefault()).isTrue
        assertThat(configuration.askForGooglePlayServices()).isFalse
        assertThat(configuration.askForSettingsApi()).isTrue
        assertThat(configuration.failOnSettingsApiSuspended()).isFalse
        assertThat(configuration.ignoreLastKnowLocation()).isFalse
        assertThat(configuration.googlePlayServicesWaitPeriod())
            .isEqualTo((20 * SECOND).toLong())
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        GooglePlayServicesConfiguration.Builder().setWaitPeriod(-1)
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration: GooglePlayServicesConfiguration =
            GooglePlayServicesConfiguration.Builder().build()
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        assertThat(firstClone.locationRequest())
            .isEqualTo(secondClone.locationRequest())
            .isEqualTo(createDefaultLocationRequest())
        assertThat(firstClone.askForGooglePlayServices())
            .isEqualTo(secondClone.askForGooglePlayServices())
            .isFalse
        assertThat(firstClone.askForSettingsApi())
            .isEqualTo(secondClone.askForSettingsApi())
            .isTrue
        assertThat(firstClone.failOnSettingsApiSuspended())
            .isEqualTo(secondClone.failOnSettingsApiSuspended())
            .isFalse
        assertThat(firstClone.ignoreLastKnowLocation())
            .isEqualTo(secondClone.ignoreLastKnowLocation())
            .isFalse
        assertThat(firstClone.googlePlayServicesWaitPeriod())
            .isEqualTo(secondClone.googlePlayServicesWaitPeriod())
            .isEqualTo((20 * SECOND).toLong())
    }

    private fun createDefaultLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval((5 * MINUTE).toLong())
            .setFastestInterval(MINUTE.toLong())
    }

    companion object {
        private const val SECOND = 1000
        private const val MINUTE = 60 * SECOND
    }
}