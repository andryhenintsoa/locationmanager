package mg.henkinn.locationmanager

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.internal.matchers.InstanceOf.VarArgAware
import org.mockito.internal.util.Primitives

fun <T : Any> safeEq(value: T): T = Mockito.eq(value) ?: value
fun <T> safeAny(type: Class<T>): T = Mockito.any<T>(type)
fun safeAnyString(): String = Mockito.anyString()