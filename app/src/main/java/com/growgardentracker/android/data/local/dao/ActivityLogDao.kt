package com.growgardentracker.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.growgardentracker.android.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY createdAt DESC")
    fun observeActivity(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentActivity(limit: Int): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllActivity()
}
