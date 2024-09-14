package me.him188.ani.app.ui.settings.framework

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue


@Immutable
enum class ConnectionTestResult {
    SUCCESS,
    FAILED,
    NOT_ENABLED
}

fun Boolean.toConnectionTestResult() =
    if (this) ConnectionTestResult.SUCCESS else ConnectionTestResult.FAILED

typealias ConnectionTester = Tester<ConnectionTestResult>

fun ConnectionTester(
    id: String,
    testConnection: suspend () -> ConnectionTestResult,
): ConnectionTester = Tester(
    id,
    testConnection,
    onError = {
        ConnectionTestResult.FAILED
    },
)

@Stable
open class Tester<T>(
    val id: String,
    private val onTest: suspend () -> T,
    private val onError: (Throwable) -> T,
) {
    var isTesting by mutableStateOf(false)
    var result: T? by mutableStateOf(null)
    var time: Duration? by mutableStateOf(null)

    fun reset() {
        isTesting = false
        result = null
        time = null
    }

    suspend fun test() {
        withContext(Dispatchers.Main) {
            isTesting = true
        }
        try {
            val (res, t) = measureTimedValue { onTest() }
            withContext(Dispatchers.Main) {
                time = t
                result = res
            }
        } catch (e: Throwable) {
            val res = onError(e)
            withContext(Dispatchers.Main) {
                time = Duration.INFINITE
                result = res
            }
            throw e
        } finally {
            // We can't use `withContext` be cause this scope has already been cancelled
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.Main) {
                isTesting = false
            }
        }
    }
}

@Composable
fun ConnectionTesterResultIndicator(
    tester: ConnectionTester,
    modifier: Modifier = Modifier,
    showTime: Boolean = false,
    showIdle: Boolean = true,
) = Box(modifier) {
    if (tester.isTesting) {
        CircularProgressIndicator(
            Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    } else {
        when (tester.result) {
            ConnectionTestResult.SUCCESS -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                    if (showTime) {
                        if (tester.time == Duration.INFINITE) {
                            Text("超时")
                        } else {
                            Text(
                                tester.time?.toString(
                                    DurationUnit.SECONDS,
                                    decimals = 2,
                                ) ?: "",
                            )
                        }
                    }
                }
            }

            ConnectionTestResult.FAILED -> {
                Icon(Icons.Rounded.Cancel, null, tint = MaterialTheme.colorScheme.error)
            }

            ConnectionTestResult.NOT_ENABLED -> {
                Text("未启用")
            }

            null -> {
                if (showIdle) {
                    Text("等待测试")
                }
            }
        }
    }
}
