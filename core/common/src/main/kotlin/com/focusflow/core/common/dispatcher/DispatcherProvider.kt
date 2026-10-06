package com.focusflow.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Injected dispatcher provider — NEVER hard-code Dispatchers.IO etc. directly.
 *
 * Rule from ARCHITECTURE.md §6 Concurrency: "Dispatchers are injected, never hard-coded."
 * Test implementations can supply TestDispatchers for deterministic coroutine testing.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher        = Dispatchers.Main
    override val io: CoroutineDispatcher          = Dispatchers.IO
    override val default: CoroutineDispatcher     = Dispatchers.Default
    override val unconfined: CoroutineDispatcher  = Dispatchers.Unconfined
}
