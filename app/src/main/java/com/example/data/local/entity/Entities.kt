package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_categories")
data class AppCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "category_apps",
    primaryKeys = ["categoryId", "packageName"]
)
data class CategoryAppCrossRef(
    val categoryId: Long,
    val packageName: String
)

@Entity(tableName = "favorite_apps")
data class FavoriteAppEntity(
    @PrimaryKey
    val packageName: String,
    val activityName: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String
)

@Entity(tableName = "custom_app_labels")
data class CustomAppLabelEntity(
    @PrimaryKey
    val packageName: String,
    val customLabel: String
)
