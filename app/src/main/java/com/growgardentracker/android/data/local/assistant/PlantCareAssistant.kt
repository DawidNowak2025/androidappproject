package com.growgardentracker.android.data.local.assistant

import com.growgardentracker.android.data.local.entity.ActivityLogEntity
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.data.local.entity.WateringHistoryEntity
import com.growgardentracker.android.util.DateUtils

object PlantCareAssistant {
    fun generateAdvice(
        plant: PlantEntity,
        zone: GardenZoneEntity?,
        wateringHistory: List<WateringHistoryEntity>,
        activityLogs: List<ActivityLogEntity>
    ): List<String> {
        val advice = mutableListOf<String>()
        val status = DateUtils.wateringStatus(plant.nextWateringDate)
        advice += "Watering status: $status. Last watered ${plant.lastWateredDate}; next watering ${plant.nextWateringDate}."

        when (status) {
            "Overdue" -> advice += "This plant looks overdue for watering based on the next watering date."
            "Water Today" -> advice += "This plant is due today. Check soil moisture before watering."
            else -> advice += "Watering appears on track. Keep monitoring the soil and leaf condition."
        }

        val noteText = "${plant.notes} ${plant.description}".lowercase()
        val warningWords = listOf("yellow leaves", "dry", "brown", "wilt", "wilting", "pests", "fungus", "slow growth")
        val matchedWarnings = warningWords.filter { noteText.contains(it) }
        if (matchedWarnings.isNotEmpty()) {
            advice += "Your notes mention ${matchedWarnings.joinToString(", ")}, so check soil moisture, light, airflow and leaf undersides."
        } else if (plant.notes.isBlank()) {
            advice += "There are no detailed notes yet. Add a note after checking leaves, soil and growth."
        }

        if (wateringHistory.isEmpty()) {
            advice += "This plant has no watering history yet, so water and care patterns are still being learned."
        } else if (wateringHistory.size >= 5) {
            advice += "Watering seems regular based on ${wateringHistory.size} watering records."
        } else {
            advice += "There are ${wateringHistory.size} watering records. Keep logging care to improve advice."
        }

        if (activityLogs.none { it.text.contains(plant.name, ignoreCase = true) }) {
            advice += "This plant has no recent activity log entry, so add a note after checking it."
        }

        zone?.let {
            advice += "This plant is in ${it.name}, so check that zone's light, temperature, airflow and access to water."
        } ?: advice.add("Assign this plant to a garden zone so care advice can include its growing location.")

        if (plant.wateringFrequencyDays <= 2) {
            advice += "The watering interval is short. Make sure the pot or bed drains well before watering again."
        } else if (plant.wateringFrequencyDays >= 10) {
            advice += "The watering interval is long. Check soil dryness during warm or windy weather."
        }

        return advice.distinct()
    }
}
