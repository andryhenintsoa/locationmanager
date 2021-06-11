package mg.henkinn.locationmanager.helper

import mg.henkinn.locationmanager.helper.logging.LogUtils
import mg.henkinn.locationmanager.helper.logging.Logger
import mg.henkinn.locationmanager.safeEq
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class LogUtilsTest {
    @Mock
    lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        LogUtils.setLogger(mockLogger)
    }

    @Test
    fun whenLoggingIsDisabledItShouldNotForwardToLogger() {
        LogUtils.enable(false)
        LogUtils.logD("Dmessage")
        LogUtils.logE("Emessage")
        LogUtils.logI("Imessage")
        LogUtils.logV("Vmessage")
        LogUtils.logW("Wmessage")
        verifyZeroInteractions(mockLogger)
    }

    @Test
    fun whenLoggingIsEnabledItShouldForwardToLogger() {
        LogUtils.enable(true)
        LogUtils.logD("Dmessage")
        LogUtils.logE("Emessage")
        LogUtils.logI("Imessage")
        LogUtils.logV("Vmessage")
        LogUtils.logW("Wmessage")
        verify(mockLogger, times(1)).logD(anyString(), safeEq("Dmessage"))
        verify(mockLogger, times(1)).logE(anyString(), safeEq("Emessage"))
        verify(mockLogger, times(1)).logI(anyString(), safeEq("Imessage"))
        verify(mockLogger, times(1)).logV(anyString(), safeEq("Vmessage"))
        verify(mockLogger, times(1)).logW(anyString(), safeEq("Wmessage"))
    }

    @Test
    fun whenChangingLoggerItShouldLogIntoIt() {
        LogUtils.enable(true)
        val newLogger: Logger = mock(Logger::class.java)
        LogUtils.setLogger(newLogger)
        LogUtils.logD("Dmessage")
        verify(newLogger, times(1)).logD(anyString(), safeEq("Dmessage"))
        verify(mockLogger, times(0)).logD(anyString(), anyString())
    }
}