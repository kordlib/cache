package com.gitlab.cord.tck

import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.RepeatedTest
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T =
        kotlinx.coroutines.runBlocking(context, block)
