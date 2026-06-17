package com.growgardentracker.android.data.repository

import android.content.Context
import com.growgardentracker.android.data.datastore.SessionDataStore
import com.growgardentracker.android.data.local.GrowDatabase
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
import com.growgardentracker.android.util.DateUtils
import com.growgardentracker.android.util.GardenBackup
import com.growgardentracker.android.util.JsonExportImportHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

sealed class ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}

class ActivityRepository(private val activityLogDao: ActivityLogDao) {
    val activity: Flow<List<ActivityLogEntity>> = activityLogDao.observeActivity()

    suspend fun log(text: String) {
        activityLogDao.insertActivity(ActivityLogEntity(date = DateUtils.today(), text = text))
    }
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionDataStore: SessionDataStore
) {
    val loggedInUserId: Flow<Long?> = sessionDataStore.loggedInUserId

    suspend fun register(name: String, email: String, password: String): ResultState<UserEntity> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return ResultState.Error("Name, email and password are required.")
        if (password.length < 6) return ResultState.Error("Password must be at least 6 characters.")
        if (userDao.getUserByEmail(email.trim().lowercase()) != null) return ResultState.Error("This email is already registered.")
        val user = UserEntity(name = name.trim(), email = email.trim().lowercase(), password = password)
        val id = userDao.insertUser(user)
        sessionDataStore.saveLoggedInUser(id)
        return ResultState.Success(user.copy(id = id))
    }

    suspend fun login(email: String, password: String): ResultState<UserEntity> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return ResultState.Error("Invalid email or password.")
        if (user.password != password) return ResultState.Error("Invalid email or password.")
        sessionDataStore.saveLoggedInUser(user.id)
        return ResultState.Success(user)
    }

    suspend fun logout() {
        sessionDataStore.clearSession()
    }

    suspend fun getLoggedInUser(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }
}

class PlantRepository(
    private val plantDao: PlantDao,
    private val historyDao: WateringHistoryDao,
    private val activityRepository: ActivityRepository
) {
    val plants: Flow<List<PlantEntity>> = plantDao.observePlants()

    fun observePlant(id: Long): Flow<PlantEntity?> = plantDao.observePlant(id)
    fun observeHistory(plantId: Long): Flow<List<WateringHistoryEntity>> = historyDao.observeHistoryForPlant(plantId)

        suspend fun addPlant(plant: PlantEntity): ResultState<Long> {
            if (plant.name.isBlank()) return ResultState.Error("Plant name is required.")
            if (plant.zoneId == null) return ResultState.Error("Garden zone is required.")
            val id = plantDao.insertPlant(plant.copy(updatedAt = System.currentTimeMillis()))
            activityRepository.log("${plant.name} plant added.")
            return ResultState.Success(id)
        }

        suspend fun updatePlant(plant: PlantEntity): ResultState<Unit> {
            if (plant.name.isBlank()) return ResultState.Error("Plant name is required.")
            if (plant.zoneId == null) return ResultState.Error("Garden zone is required.")
            plantDao.updatePlant(plant.copy(updatedAt = System.currentTimeMillis()))
            activityRepository.log("${plant.name} plant edited.")
            return ResultState.Success(Unit)
        }

    suspend fun deletePlant(plant: PlantEntity) {
        plantDao.deletePlant(plant)
        activityRepository.log("${plant.name} plant deleted.")
    }

    suspend fun waterToday(plant: PlantEntity) {
        val today = DateUtils.today()
        val nextDate = DateUtils.nextWateringDate(today, plant.wateringFrequencyDays)
        plantDao.updatePlant(plant.copy(lastWateredDate = today, nextWateringDate = nextDate, updatedAt = System.currentTimeMillis()))
        historyDao.insertHistory(WateringHistoryEntity(plantId = plant.id, date = today, note = "Plant watered."))
        activityRepository.log("${plant.name} plant watered.")
    }

    suspend fun addHistory(plantId: Long, date: String, note: String) {
        historyDao.insertHistory(WateringHistoryEntity(plantId = plantId, date = date.ifBlank { DateUtils.today() }, note = note.ifBlank { "No note added." }))
    }
}

class ZoneRepository(
    private val zoneDao: ZoneDao,
    private val plantDao: PlantDao,
    private val activityRepository: ActivityRepository
) {
    val zones: Flow<List<GardenZoneEntity>> = zoneDao.observeZones()

    suspend fun getAllZones(): List<GardenZoneEntity> = zoneDao.getAllZones()

    suspend fun getZoneById(id: Long): GardenZoneEntity? = zoneDao.getZoneById(id)

    suspend fun addZone(name: String, description: String, imagePath: String?): ResultState<Unit> {
        if (name.isBlank()) return ResultState.Error("Garden zone name is required.")
        if (zoneDao.getZoneByName(name.trim()) != null) return ResultState.Error("A garden zone with this name already exists.")
        zoneDao.insertZone(
            GardenZoneEntity(
                name = name.trim(),
                description = description.trim().ifBlank { null },
                imagePath = imagePath
            )
        )
        activityRepository.log("Zone added: ${name.trim()}.")
        return ResultState.Success(Unit)
    }

    suspend fun updateZone(zone: GardenZoneEntity, newName: String, description: String, imagePath: String?): ResultState<Unit> {
        if (newName.isBlank()) return ResultState.Error("Garden zone name is required.")
        val duplicate = zoneDao.getZoneByName(newName.trim())
        if (duplicate != null && duplicate.id != zone.id) return ResultState.Error("A garden zone with this name already exists.")
        val photoChanged = zone.imagePath != imagePath
        zoneDao.updateZone(
            zone.copy(
                name = newName.trim(),
                description = description.trim().ifBlank { null },
                imagePath = imagePath,
                updatedAt = System.currentTimeMillis()
            )
        )
        activityRepository.log("Zone edited: ${zone.name} to ${newName.trim()}.")
        if (photoChanged) activityRepository.log("Zone photo updated: ${newName.trim()}.")
        return ResultState.Success(Unit)
    }

    suspend fun deleteZone(zone: GardenZoneEntity): ResultState<Unit> {
        val plantCount = plantDao.countPlantsInZone(zone.id)
        if (plantCount > 0) {
            activityRepository.log("Zone delete blocked because plants exist: ${zone.name}.")
            return ResultState.Error("This zone has plants assigned.")
        }
        zoneDao.deleteZone(zone)
        activityRepository.log("Zone deleted: ${zone.name}.")
        return ResultState.Success(Unit)
    }
}

data class DashboardSummary(
    val totalPlants: Int = 0,
    val overduePlants: Int = 0,
    val nextWateringPlants: Int = 0,
    val recentActivity: List<ActivityLogEntity> = emptyList()
)

class DashboardRepository(
    private val plantDao: PlantDao,
    private val activityLogDao: ActivityLogDao
) {
    val summary: Flow<DashboardSummary> = combine(
        plantDao.observePlants(),
        activityLogDao.observeRecentActivity(5)
    ) { plants, activity ->
        DashboardSummary(
            totalPlants = plants.size,
            overduePlants = plants.count { DateUtils.wateringStatus(it.nextWateringDate) == "Overdue" },
            nextWateringPlants = plants.count { DateUtils.wateringStatus(it.nextWateringDate) == "Water Today" || DateUtils.wateringStatus(it.nextWateringDate) == "Upcoming" },
            recentActivity = activity
        )
    }
}

class SettingsRepository(
    private val database: GrowDatabase,
    private val activityRepository: ActivityRepository,
    private val context: Context
) {
    suspend fun exportJson(): ResultState<String> {
        val backup = GardenBackup(
            plants = database.plantDao().observePlants().first(),
            zones = database.zoneDao().observeZones().first(),
            wateringHistory = database.wateringHistoryDao().observeAllHistory().first(),
            activityLogs = database.activityLogDao().observeActivity().first()
        )
        val path = JsonExportImportHelper.writeBackup(context, backup)
        activityRepository.log("JSON exported.")
        return ResultState.Success(path)
    }

    suspend fun importJson(): ResultState<Unit> {
        val backup = JsonExportImportHelper.readBackup(context) ?: return ResultState.Error("No backup file found in app storage.")
        database.plantDao().deleteAllPlants()
        database.zoneDao().deleteAllZones()
        database.activityLogDao().deleteAllActivity()
        backup.zones.forEach { database.zoneDao().insertZone(it) }
        backup.plants.forEach { database.plantDao().insertPlant(it) }
        backup.wateringHistory.forEach { database.wateringHistoryDao().insertHistory(it) }
        backup.activityLogs.forEach { database.activityLogDao().insertActivity(it) }
        activityRepository.log("JSON imported.")
        return ResultState.Success(Unit)
    }
}
