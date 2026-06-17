package com.growgardentracker.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.growgardentracker.android.data.local.dao.ActivityLogDao
import com.growgardentracker.android.data.local.dao.PlantDao
import com.growgardentracker.android.data.local.dao.UserDao
import com.growgardentracker.android.data.local.dao.WateringHistoryDao
import com.growgardentracker.android.data.local.dao.ZoneDao
import com.growgardentracker.android.data.local.entity.ActivityLogEntity
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.data.local.entity.UserEntity
import com.growgardentracker.android.data.local.entity.WateringHistoryEntity

@Database(
    entities = [
        UserEntity::class,
        PlantEntity::class,
        GardenZoneEntity::class,
        WateringHistoryEntity::class,
        ActivityLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GrowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun plantDao(): PlantDao
    abstract fun zoneDao(): ZoneDao
    abstract fun wateringHistoryDao(): WateringHistoryDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile private var INSTANCE: GrowDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN mapX REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE plants ADD COLUMN mapY REAL DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS garden_zones_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT DEFAULT NULL,
                        imagePath TEXT DEFAULT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO garden_zones_new (id, name, description, imagePath, createdAt, updatedAt)
                    SELECT id, name, NULL, imagePath, createdAt, updatedAt FROM garden_zones
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE garden_zones")
                database.execSQL("ALTER TABLE garden_zones_new RENAME TO garden_zones")
            }
        }

        fun getDatabase(context: Context): GrowDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GrowDatabase::class.java,
                    "grow_garden_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
