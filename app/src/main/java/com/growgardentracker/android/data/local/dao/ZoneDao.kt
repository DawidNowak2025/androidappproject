package com.growgardentracker.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Query("SELECT * FROM garden_zones ORDER BY name ASC")
    fun observeZones(): Flow<List<GardenZoneEntity>>

    @Query("SELECT * FROM garden_zones ORDER BY name ASC")
    suspend fun getAllZones(): List<GardenZoneEntity>

    @Query("SELECT * FROM garden_zones WHERE id = :id LIMIT 1")
    suspend fun getZone(id: Long): GardenZoneEntity?

    @Query("SELECT * FROM garden_zones WHERE id = :id LIMIT 1")
    suspend fun getZoneById(id: Long): GardenZoneEntity?

    @Query("SELECT * FROM garden_zones WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getZoneByName(name: String): GardenZoneEntity?

    @Query("SELECT COUNT(*) FROM garden_zones")
    suspend fun countZones(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: GardenZoneEntity): Long

    @Update
    suspend fun updateZone(zone: GardenZoneEntity)

    @Delete
    suspend fun deleteZone(zone: GardenZoneEntity)

    @Query("DELETE FROM garden_zones")
    suspend fun deleteAllZones()
}
