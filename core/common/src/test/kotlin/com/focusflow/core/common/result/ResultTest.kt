package com.focusflow.core.common.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun success_containsData_andInvokesOnSuccess() {
        val result: Result<String> = Result.Success("focus")
        var received = ""

        result.onSuccess { received = it }

        assertEquals("focus", received)
        assertEquals("focus", result.getOrNull())
    }

    @Test
    fun error_containsException_andInvokesOnError() {
        val error = IllegalStateException("test error")
        val result: Result<String> = Result.Error(error)
        var receivedError: Throwable? = null

        result.onError { receivedError = it }

        assertEquals(error, receivedError)
        assertNull(result.getOrNull())
    }

    @Test
    fun loading_invokesOnLoading() {
        val result: Result<String> = Result.Loading
        var loadingCalled = false

        result.onLoading { loadingCalled = true }

        assertTrue(loadingCalled)
        assertNull(result.getOrNull())
    }
}
