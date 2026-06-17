package com.growgardentracker.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.growgardentracker.android.data.local.entity.WateringHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WateringHistoryDao {
    @Query("SELECT * FROM watering_history WHERE plantId = :plantId ORDER BY date DESC, createdAt DESC")
    fun observeHistoryForPlant(plantId: Long): Flow<List<WateringHistoryEntity>>

    @Query("SELECT * FROM watering_history ORDER BY createdAt DESC")
    fun observeAllHistory(): Flow<List<WateringHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WateringHistoryEntity): Long

    @Query("DELETE FROM watering_history")
    suspend fun deleteAllHistory()
}
