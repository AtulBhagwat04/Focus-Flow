package com.focusflow.core.domain.sync.model

sealed interface SyncResult {
    data object Success : SyncResult
    data class Failure(val errorMessage: String) : SyncResult
}
