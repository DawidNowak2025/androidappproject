package com.growgardentracker.android.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.growgardentracker.android.data.local.entity.ActivityLogEntity
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.data.local.entity.WateringHistoryEntity
import java.io.File

data class GardenBackup(
    val plants: List<PlantEntity>,
    val zones: List<GardenZoneEntity>,
    val wateringHistory: List<WateringHistoryEntity>,
    val activityLogs: List<ActivityLogEntity>
)

object JsonExportImportHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun backupFile(context: Context): File {
        return File(context.filesDir, "grow_garden_backup.json")
    }

    fun writeBackup(context: Context, backup: GardenBackup): String {
        val file = backupFile(context)
        file.writeText(gson.toJson(backup))
        return file.absolutePath
    }

    fun readBackup(context: Context): GardenBackup? {
        val file = backupFile(context)
        if (!file.exists()) return null
        return gson.fromJson(file.readText(), GardenBackup::class.java)
    }
}
