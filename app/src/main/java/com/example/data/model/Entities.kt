package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "blood_banks")
data class BloodBankEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val verified: Boolean = true,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blood_stock",
    indices = [Index(value = ["bankId", "bloodGroup", "component"], unique = true)]
)
data class BloodStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankId: Long,
    val bloodGroup: String,
    val component: String,
    val units: Int,
    val expiryDaysLeft: Int,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val age: Int,
    val bloodGroup: String,
    val city: String,
    val phone: String,
    val email: String,
    val lastDonationEpochDay: Long, // Days since Unix epoch
    val isAvailable: Boolean = true,
    val consentMaskPhone: Boolean = true,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val totalDonationsCount: Int = 1
) {
    /**
     * Auto-mark donor ineligible if they donated in the last 90 days or if age not between 18 and 65.
     */
    fun isEligibleToDonate(): Boolean {
        if (age < 18 || age > 65) return false
        val currentDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val daysSinceLastDonation = currentDay - lastDonationEpochDay
        return daysSinceLastDonation >= 90
    }

    fun daysUntilEligible(): Long {
        if (age < 18 || age > 65) return -1
        val currentDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val daysSinceLastDonation = currentDay - lastDonationEpochDay
        return if (daysSinceLastDonation >= 90) 0 else (90 - daysSinceLastDonation)
    }

    fun maskedPhone(): String {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        return if (clean.length > 6) {
            val prefix = clean.take(4)
            val suffix = clean.takeLast(2)
            "$prefix •••• $suffix"
        } else {
            "••• ••• •••"
        }
    }
}

@Entity(tableName = "emergency_requests")
data class EmergencyRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val bloodGroup: String,
    val component: String = BloodComponent.WHOLE_BLOOD.displayName,
    val unitsNeeded: Int = 2,
    val hospitalName: String,
    val hospitalAddress: String,
    val city: String,
    val urgency: String = UrgencyLevel.CRITICAL.name,
    val contactPerson: String,
    val contactPhone: String,
    val status: String = RequestStatus.OPEN.name,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val matchingDonorsAlerted: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "donation_history")
data class DonationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val donorId: Long,
    val dateEpochMillis: Long,
    val bloodBankOrHospital: String,
    val component: String,
    val unitsDonated: Int
)
