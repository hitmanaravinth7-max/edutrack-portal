package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BloodBankEntity
import com.example.data.model.BloodComponent
import com.example.data.model.BloodGroup
import com.example.data.model.BloodStockEntity
import com.example.data.model.DonationHistoryEntity
import com.example.data.model.DonorEntity
import com.example.data.model.EmergencyRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UrgencyLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Database(
    entities = [
        BloodBankEntity::class,
        BloodStockEntity::class,
        DonorEntity::class,
        EmergencyRequestEntity::class,
        DonationHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lifeLinkDao(): LifeLinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifelink_blood_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.lifeLinkDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: LifeLinkDao) {
            val now = System.currentTimeMillis()
            val currentDay = TimeUnit.MILLISECONDS.toDays(now)

            // 1. Initial Blood Banks
            val banks = listOf(
                BloodBankEntity(
                    id = 1,
                    name = "Metro Central Blood & Trauma Center",
                    city = "Downtown Metro",
                    address = "104 Healthcare Blvd, Downtown Metro",
                    phone = "+1 (555) 234-5678",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    verified = true,
                    lastUpdatedMillis = now - 15 * 60 * 1000
                ),
                BloodBankEntity(
                    id = 2,
                    name = "St. Jude Red Cross Regional Blood Bank",
                    city = "West Hills",
                    address = "520 Hope Avenue, West Hills",
                    phone = "+1 (555) 345-6789",
                    latitude = 37.7833,
                    longitude = -122.4467,
                    verified = true,
                    lastUpdatedMillis = now - 45 * 60 * 1000
                ),
                BloodBankEntity(
                    id = 3,
                    name = "City General Hospital Blood Bank",
                    city = "Northside",
                    address = "890 North Medical Pkwy, Northside",
                    phone = "+1 (555) 456-7890",
                    latitude = 37.7989,
                    longitude = -122.4089,
                    verified = true,
                    lastUpdatedMillis = now - 120 * 60 * 1000
                ),
                BloodBankEntity(
                    id = 4,
                    name = "Mercy Community Blood & Platelet Center",
                    city = "South Bay",
                    address = "312 Harbor View Rd, South Bay",
                    phone = "+1 (555) 567-8901",
                    latitude = 37.7420,
                    longitude = -122.4350,
                    verified = true,
                    lastUpdatedMillis = now - 30 * 60 * 1000
                ),
                BloodBankEntity(
                    id = 5,
                    name = "Apex Lifeline Transfusion Institute",
                    city = "East Valley",
                    address = "77 Sunrise Blvd, East Valley",
                    phone = "+1 (555) 678-9012",
                    latitude = 37.7650,
                    longitude = -122.3950,
                    verified = true,
                    lastUpdatedMillis = now - 200 * 60 * 1000
                )
            )
            dao.insertAllBloodBanks(banks)

            // 2. Initial Stock per Bank
            val initialStock = mutableListOf<BloodStockEntity>()
            val groups = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
            val components = listOf(
                BloodComponent.WHOLE_BLOOD.displayName,
                BloodComponent.PLASMA.displayName,
                BloodComponent.PLATELETS.displayName
            )

            // Seed stock with realistic variation including critical low stock
            banks.forEach { bank ->
                groups.forEach { group ->
                    components.forEach { comp ->
                        val baseUnits = when {
                            group == "O-" -> if (bank.id == 1L) 3 else 1 // Low stock for rare O-
                            group == "AB-" -> 2
                            comp == BloodComponent.PLATELETS.displayName -> if (bank.id == 2L) 4 else 8
                            else -> ((bank.id * 3 + group.hashCode()) % 15).toInt().coerceAtLeast(2) + 4
                        }
                        val expiryDays = when (comp) {
                            BloodComponent.PLATELETS.displayName -> (1..4).random()
                            BloodComponent.PLASMA.displayName -> (180..340).random()
                            else -> (10..32).random()
                        }
                        initialStock.add(
                            BloodStockEntity(
                                bankId = bank.id,
                                bloodGroup = group,
                                component = comp,
                                units = baseUnits,
                                expiryDaysLeft = expiryDays,
                                lastUpdatedMillis = bank.lastUpdatedMillis
                            )
                        )
                    }
                }
            }
            dao.insertAllStock(initialStock)

            // 3. Initial Registered Donors
            val donors = listOf(
                DonorEntity(
                    id = 1,
                    fullName = "Alex Rivera",
                    age = 28,
                    bloodGroup = "O-",
                    city = "Downtown Metro",
                    phone = "+1 (555) 789-0123",
                    email = "alex.rivera@lifelink.org",
                    lastDonationEpochDay = currentDay - 110, // Eligible
                    isAvailable = true,
                    consentMaskPhone = true,
                    latitude = 37.7760,
                    longitude = -122.4200,
                    totalDonationsCount = 6
                ),
                DonorEntity(
                    id = 2,
                    fullName = "Priya Sharma",
                    age = 24,
                    bloodGroup = "A+",
                    city = "Downtown Metro",
                    phone = "+1 (555) 890-1234",
                    email = "priya.sharma@lifelink.org",
                    lastDonationEpochDay = currentDay - 45, // Ineligible (45 days left)
                    isAvailable = false,
                    consentMaskPhone = true,
                    latitude = 37.7780,
                    longitude = -122.4150,
                    totalDonationsCount = 3
                ),
                DonorEntity(
                    id = 3,
                    fullName = "Carlos Mendez",
                    age = 34,
                    bloodGroup = "B+",
                    city = "West Hills",
                    phone = "+1 (555) 901-2345",
                    email = "carlos.m@lifelink.org",
                    lastDonationEpochDay = currentDay - 130, // Eligible
                    isAvailable = true,
                    consentMaskPhone = true,
                    latitude = 37.7850,
                    longitude = -122.4400,
                    totalDonationsCount = 8
                ),
                DonorEntity(
                    id = 4,
                    fullName = "Sarah Jenkins",
                    age = 29,
                    bloodGroup = "O+",
                    city = "Northside",
                    phone = "+1 (555) 012-3456",
                    email = "sarah.j@lifelink.org",
                    lastDonationEpochDay = currentDay - 98, // Eligible
                    isAvailable = true,
                    consentMaskPhone = false,
                    latitude = 37.8010,
                    longitude = -122.4100,
                    totalDonationsCount = 4
                ),
                DonorEntity(
                    id = 5,
                    fullName = "David Chen",
                    age = 31,
                    bloodGroup = "AB+",
                    city = "South Bay",
                    phone = "+1 (555) 123-4567",
                    email = "david.chen@lifelink.org",
                    lastDonationEpochDay = currentDay - 150, // Eligible
                    isAvailable = true,
                    consentMaskPhone = true,
                    latitude = 37.7450,
                    longitude = -122.4300,
                    totalDonationsCount = 5
                ),
                DonorEntity(
                    id = 6,
                    fullName = "Fatima Al-Mansoor",
                    age = 26,
                    bloodGroup = "A-",
                    city = "East Valley",
                    phone = "+1 (555) 234-9876",
                    email = "fatima.m@lifelink.org",
                    lastDonationEpochDay = currentDay - 102, // Eligible
                    isAvailable = true,
                    consentMaskPhone = true,
                    latitude = 37.7680,
                    longitude = -122.3900,
                    totalDonationsCount = 2
                ),
                DonorEntity(
                    id = 7,
                    fullName = "Michael Chang",
                    age = 39,
                    bloodGroup = "B-",
                    city = "Downtown Metro",
                    phone = "+1 (555) 345-8765",
                    email = "m.chang@lifelink.org",
                    lastDonationEpochDay = currentDay - 25, // Ineligible (65 days left)
                    isAvailable = false,
                    consentMaskPhone = true,
                    latitude = 37.7710,
                    longitude = -122.4250,
                    totalDonationsCount = 11
                ),
                DonorEntity(
                    id = 8,
                    fullName = "Elena Rostova",
                    age = 27,
                    bloodGroup = "AB-",
                    city = "West Hills",
                    phone = "+1 (555) 456-7654",
                    email = "elena.r@lifelink.org",
                    lastDonationEpochDay = currentDay - 140, // Eligible
                    isAvailable = true,
                    consentMaskPhone = true,
                    latitude = 37.7810,
                    longitude = -122.4500,
                    totalDonationsCount = 7
                )
            )
            dao.insertAllDonors(donors)

            // 4. Initial Emergency Requests
            val requests = listOf(
                EmergencyRequestEntity(
                    id = 1,
                    patientName = "Robert Taylor",
                    bloodGroup = "O-",
                    component = BloodComponent.WHOLE_BLOOD.displayName,
                    unitsNeeded = 3,
                    hospitalName = "City General Trauma Center",
                    hospitalAddress = "890 North Medical Pkwy, Northside",
                    city = "Northside",
                    urgency = UrgencyLevel.CRITICAL.name,
                    contactPerson = "Dr. Katherine Shaw (ER Chief)",
                    contactPhone = "+1 (555) 456-7890",
                    status = RequestStatus.OPEN.name,
                    createdAtMillis = now - 35 * 60 * 1000,
                    matchingDonorsAlerted = 14,
                    notes = "Immediate trauma surgery required post multi-vehicle collision."
                ),
                EmergencyRequestEntity(
                    id = 2,
                    patientName = "Maya Patel",
                    bloodGroup = "B+",
                    component = BloodComponent.WHOLE_BLOOD.displayName,
                    unitsNeeded = 2,
                    hospitalName = "Metro Central Hospital",
                    hospitalAddress = "104 Healthcare Blvd, Downtown Metro",
                    city = "Downtown Metro",
                    urgency = UrgencyLevel.WITHIN_24H.name,
                    contactPerson = "Aarav Patel (Brother)",
                    contactPhone = "+1 (555) 998-1122",
                    status = RequestStatus.OPEN.name,
                    createdAtMillis = now - 3 * 3600 * 1000,
                    matchingDonorsAlerted = 28,
                    notes = "Regular Thalassemia transfusion needed before tomorrow morning."
                ),
                EmergencyRequestEntity(
                    id = 3,
                    patientName = "Liam Gallagher",
                    bloodGroup = "A-",
                    component = BloodComponent.PLATELETS.displayName,
                    unitsNeeded = 2,
                    hospitalName = "Mercy Community Hospital",
                    hospitalAddress = "312 Harbor View Rd, South Bay",
                    city = "South Bay",
                    urgency = UrgencyLevel.CRITICAL.name,
                    contactPerson = "Nurse Sarah Miller",
                    contactPhone = "+1 (555) 567-8901",
                    status = RequestStatus.OPEN.name,
                    createdAtMillis = now - 55 * 60 * 1000,
                    matchingDonorsAlerted = 19,
                    notes = "Severe thrombocytopenia patient undergoing urgent oncology treatment."
                ),
                EmergencyRequestEntity(
                    id = 4,
                    patientName = "Marcus Vance",
                    bloodGroup = "A+",
                    component = BloodComponent.WHOLE_BLOOD.displayName,
                    unitsNeeded = 1,
                    hospitalName = "St. Jude Hospital",
                    hospitalAddress = "520 Hope Avenue, West Hills",
                    city = "West Hills",
                    urgency = UrgencyLevel.PLANNED.name,
                    contactPerson = "Jessica Vance (Spouse)",
                    contactPhone = "+1 (555) 345-6789",
                    status = RequestStatus.FULFILLED.name,
                    createdAtMillis = now - 24 * 3600 * 1000,
                    matchingDonorsAlerted = 32,
                    notes = "Cardiovascular bypass surgery scheduled. Units secured."
                )
            )
            dao.insertAllEmergencyRequests(requests)

            // 5. Initial Donation History for Donor 1 (Alex Rivera)
            val history = listOf(
                DonationHistoryEntity(
                    id = 1,
                    donorId = 1,
                    dateEpochMillis = now - (110L * 24 * 3600 * 1000),
                    bloodBankOrHospital = "Metro Central Blood Bank",
                    component = "Whole Blood",
                    unitsDonated = 1
                ),
                DonationHistoryEntity(
                    id = 2,
                    donorId = 1,
                    dateEpochMillis = now - (220L * 24 * 3600 * 1000),
                    bloodBankOrHospital = "City General Trauma Center",
                    component = "Whole Blood",
                    unitsDonated = 1
                ),
                DonationHistoryEntity(
                    id = 3,
                    donorId = 1,
                    dateEpochMillis = now - (340L * 24 * 3600 * 1000),
                    bloodBankOrHospital = "St. Jude Red Cross",
                    component = "Whole Blood",
                    unitsDonated = 1
                )
            )
            dao.insertAllDonationHistory(history)
        }
    }
}
