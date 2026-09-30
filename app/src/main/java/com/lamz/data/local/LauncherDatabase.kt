package com.lamz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lamz.data.local.dao.LauncherDao
import com.lamz.data.local.entity.AppCategoryEntity
import com.lamz.data.local.entity.CategoryAppCrossRef
import com.lamz.data.local.entity.CustomAppLabelEntity
import com.lamz.data.local.entity.FavoriteAppEntity
import com.lamz.data.local.entity.HiddenAppEntity

@Database(
    entities = [
        AppCategoryEntity::class,
        CategoryAppCrossRef::class,
        FavoriteAppEntity::class,
        HiddenAppEntity::class,
        CustomAppLabelEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {

    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "minimalos_launcher.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
