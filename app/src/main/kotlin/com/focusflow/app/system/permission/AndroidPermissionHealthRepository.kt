package com.focusflow.app.system.permission

import com.focusflow.core.domain.permission.PermissionHealth
import com.focusflow.core.domain.permission.PermissionHealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [PermissionHealthRepository] backed by [AndroidPermissionChecker].
 */
@Singleton
class AndroidPermissionHealthRepository @Inject constructor(
    private val checker: AndroidPermissionChecker,
) : PermissionHealthRepository {

    private val _permissionHealth = MutableStateFlow(
        PermissionHealth(statuses = checker.checkAll())
    )

    override val permissionHealth: Flow<PermissionHealth> = _permissionHealth.asStateFlow()

    override suspend fun refreshHealth() {
        _permissionHealth.value = PermissionHealth(statuses = checker.checkAll())
    }
}
