package com.lamz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lamz.data.local.entity.AppCategoryEntity
import com.lamz.data.local.entity.CategoryAppCrossRef
import com.lamz.data.local.entity.CustomAppLabelEntity
import com.lamz.data.local.entity.FavoriteAppEntity
import com.lamz.data.local.entity.HiddenAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {

    // --- FAVORITES ---
    @Query("SELECT * FROM favorite_apps ORDER BY orderIndex ASC")
    fun getAllFavorites(): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteAppEntity)

    @Query("DELETE FROM favorite_apps WHERE packageName = :packageName")
    suspend fun deleteFavorite(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_apps WHERE packageName = :packageName)")
    suspend fun isFavorite(packageName: String): Boolean

    // --- HIDDEN APPS ---
    @Query("SELECT * FROM hidden_apps")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenApp(hidden: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun deleteHiddenApp(packageName: String)

    // --- CUSTOM LABELS ---
    @Query("SELECT * FROM custom_app_labels")
    fun getAllCustomLabels(): Flow<List<CustomAppLabelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLabel(label: CustomAppLabelEntity)

    @Query("DELETE FROM custom_app_labels WHERE packageName = :packageName")
    suspend fun deleteCustomLabel(packageName: String)

    // --- CATEGORIES ---
    @Query("SELECT * FROM app_categories ORDER BY orderIndex ASC, name ASC")
    fun getAllCategories(): Flow<List<AppCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: AppCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: AppCategoryEntity)

    @Query("DELETE FROM app_categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    // --- CATEGORY APP ASSOCIATIONS ---
    @Query("SELECT * FROM category_apps")
    fun getAllCategoryApps(): Flow<List<CategoryAppCrossRef>>

    @Query("SELECT packageName FROM category_apps WHERE categoryId = :categoryId")
    fun getAppsForCategory(categoryId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAppToCategory(crossRef: CategoryAppCrossRef)

    @Query("DELETE FROM category_apps WHERE categoryId = :categoryId AND packageName = :packageName")
    suspend fun removeAppFromCategory(categoryId: Long, packageName: String)

    @Query("DELETE FROM category_apps WHERE categoryId = :categoryId")
    suspend fun removeAllAppsForCategory(categoryId: Long)

    @Query("DELETE FROM category_apps WHERE packageName = :packageName")
    suspend fun removeAppFromAllCategories(packageName: String)

    @Transaction
    suspend fun deleteCategoryAndAssociations(categoryId: Long) {
        removeAllAppsForCategory(categoryId)
        deleteCategory(categoryId)
    }
}
