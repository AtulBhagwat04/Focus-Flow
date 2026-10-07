package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_rules")
data class BlockRuleEntity(
    @PrimaryKey
    val packageName: String,
    val blockDuringFocus: Boolean,
    val isAlwaysBlocked: Boolean,
)
