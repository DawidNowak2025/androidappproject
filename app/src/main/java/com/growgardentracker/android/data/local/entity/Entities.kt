package com.growgardentracker.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "garden_zones")
data class GardenZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "plants",
    foreignKeys = [
        ForeignKey(
            entity = GardenZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["zoneId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("zoneId")]
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val plantType: String,
    val zoneId: Long?,
    val description: String,
    val plantedDate: String,
    val lastWateredDate: String,
    val nextWateringDate: String,
    val wateringFrequencyDays: Int,
        val notes: String,
        val plantImagePath: String = "",
        val locationImagePath: String = "",
        val mapX: Float? = null,
        val mapY: Float? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

@Entity(
    tableName = "watering_history",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantId")]
)
data class WateringHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val date: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
