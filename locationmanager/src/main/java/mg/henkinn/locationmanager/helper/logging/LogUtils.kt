package mg.henkinn.locationmanager.helper.logging


object LogUtils {
    private var isEnabled = false
    private var activeLogger: Logger = DefaultLogger()
    fun enable(isEnabled: Boolean) {
        LogUtils.isEnabled = isEnabled
    }

    fun setLogger(logger: Logger) {
        activeLogger = logger
    }

    fun logD(message: String) {
        if (isEnabled) activeLogger.logD(className, message)
    }

    fun logE(message: String) {
        if (isEnabled) activeLogger.logE(className, message)
    }

    fun logI(message: String) {
        if (isEnabled) activeLogger.logI(className, message)
    }

    fun logV(message: String) {
        if (isEnabled) activeLogger.logV(className, message)
    }

    fun logW(message: String) {
        if (isEnabled) activeLogger.logW(className, message)
    }

    private val className: String
        get() {
            val trace = Thread.currentThread().stackTrace
            val relevantTrace = trace[4]
            val className = relevantTrace.className
            val lastIndex = className.lastIndexOf('.')
            return className.substring(lastIndex + 1)
        }
}