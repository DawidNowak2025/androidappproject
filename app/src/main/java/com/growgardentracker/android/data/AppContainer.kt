package com.growgardentracker.android.data

import android.content.Context
import com.growgardentracker.android.data.datastore.SessionDataStore
import com.growgardentracker.android.data.local.GrowDatabase
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.repository.ActivityRepository
import com.growgardentracker.android.data.repository.AuthRepository
import com.growgardentracker.android.data.repository.DashboardRepository
import com.growgardentracker.android.data.repository.PlantRepository
import com.growgardentracker.android.data.repository.SettingsRepository
import com.growgardentracker.android.data.repository.ZoneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppContainer {
    lateinit var database: GrowDatabase
    lateinit var sessionDataStore: SessionDataStore
    lateinit var authRepository: AuthRepository
    lateinit var plantRepository: PlantRepository
    lateinit var zoneRepository: ZoneRepository
    lateinit var activityRepository: ActivityRepository
    lateinit var dashboardRepository: DashboardRepository
    lateinit var settingsRepository: SettingsRepository

    fun init(context: Context) {
        if (::database.isInitialized) return
        val appContext = context.applicationContext
        database = GrowDatabase.getDatabase(appContext)
        sessionDataStore = SessionDataStore(appContext)
        activityRepository = ActivityRepository(database.activityLogDao())
            zoneRepository = ZoneRepository(database.zoneDao(), database.plantDao(), activityRepository)
        plantRepository = PlantRepository(database.plantDao(), database.wateringHistoryDao(), activityRepository)
        authRepository = AuthRepository(database.userDao(), sessionDataStore)
        dashboardRepository = DashboardRepository(database.plantDao(), database.activityLogDao())
        settingsRepository = SettingsRepository(database, activityRepository, appContext)
        seedDefaultZones()
    }

    private fun seedDefaultZones() {
        CoroutineScope(Dispatchers.IO).launch {
            if (database.zoneDao().countZones() == 0) {
                val defaults = listOf(
                    "Back Garden - Left Bed",
                    "Back Garden - Patio Right",
                    "Greenhouse",
                    "Patio Shed",
                    "Front Garden",
                    "Indoor Plants"
                )
                defaults.forEach { database.zoneDao().insertZone(GardenZoneEntity(name = it)) }
            }
        }
    }
}
