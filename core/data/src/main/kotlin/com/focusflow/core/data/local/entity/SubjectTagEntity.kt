package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusflow.core.domain.model.SubjectTag

@Entity(tableName = "subject_tags")
data class SubjectTagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String = "timer",
    val isDefault: Boolean = false,
)

fun SubjectTagEntity.asDomainModel(): SubjectTag = SubjectTag(
    id = id,
    name = name,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault,
)

fun SubjectTag.asEntity(): SubjectTagEntity = SubjectTagEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault,
)
