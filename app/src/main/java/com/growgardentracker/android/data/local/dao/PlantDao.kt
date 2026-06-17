package com.growgardentracker.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.growgardentracker.android.data.local.entity.PlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY name ASC")
    fun observePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    fun observePlant(id: Long): Flow<PlantEntity?>

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    suspend fun getPlant(id: Long): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()

    @Query("SELECT COUNT(*) FROM plants WHERE zoneId = :zoneId")
    suspend fun countPlantsInZone(zoneId: Long): Int
}
