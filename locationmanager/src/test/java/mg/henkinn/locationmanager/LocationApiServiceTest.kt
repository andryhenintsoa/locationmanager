package mg.henkinn.locationmanager

import android.location.Location
import mg.henkinn.locationmanager.service.ApiServiceListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.assertj.core.api.Assertions.assertThat


@RunWith(MockitoJUnitRunner::class)
class LocationApiServiceTest {

    private val server = MockWebServer()

    @Mock
    lateinit var listener: ApiServiceListener

    @Before
    fun setup() {
        server.start()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun dataFromServerShouldBeTheSameAfterParsing() {
        val location = Location("foo")
        location.latitude = 10.0
        location.longitude = 10.0

        val expectedResponseCode = 500
        val expectedResponse = MockResponse()
            .setBody("testDataJson")
            .setResponseCode(expectedResponseCode)
        server.enqueue(expectedResponse)

        val service = Mockito.spy(LocationApiService(getUrl(), listener))

        val response = service.postSync(location)

        server.takeRequest()

        assertThat(response.body).isEqualTo(expectedResponse.body?.readUtf8())
        assertThat(response.code).isEqualTo(expectedResponseCode)
        assertThat(response.message).isSubstringOf(expectedResponse.status)
        expectedResponse.headers.names().forEach {
            assertThat(response.headers).contains(Pair(it, expectedResponse.headers[it]!!))
        }
    }

    private fun getUrl() = server.url("/").url().toString()
}