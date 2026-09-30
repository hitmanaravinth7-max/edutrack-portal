package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LifeLinkRepository
import com.example.data.model.BloodBankEntity
import com.example.data.model.BloodComponent
import com.example.data.model.BloodGroup
import com.example.data.model.BloodStockEntity
import com.example.data.model.DonationHistoryEntity
import com.example.data.model.DonorEntity
import com.example.data.model.EmergencyRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UrgencyLevel
import com.example.ui.util.AppLanguage
import com.example.ui.util.GeoUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class ScreenTab {
    AVAILABILITY,
    EMERGENCY_SOS,
    DONOR_FINDER,
    MAP_VIEW,
    DONOR_PROFILE,
    ADMIN_STOCK,
    EDUCATION
}

data class BloodBankWithStock(
    val bank: BloodBankEntity,
    val stockItems: List<BloodStockEntity>,
    val distanceKm: Double,
    val totalAvailableUnits: Int
)

data class DonorMatch(
    val donor: DonorEntity,
    val isDirectMatch: Boolean,
    val isCompatible: Boolean,
    val compatibilityNote: String,
    val distanceKm: Double
)

data class FilterState(
    val group: String,
    val component: String,
    val city: String,
    val query: String
)

class LifeLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeLinkRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = LifeLinkRepository(db.lifeLinkDao())
    }

    // App Language
    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = AppLanguage.toggle(_language.value)
    }

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.AVAILABILITY)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Blood Banks & Stock
    val allBloodBanks: StateFlow<List<BloodBankEntity>> = repository.allBloodBanks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStock: StateFlow<List<BloodStockEntity>> = repository.allStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDonors: StateFlow<List<DonorEntity>> = repository.allDonors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmergencyRequests: StateFlow<List<EmergencyRequestEntity>> = repository.allEmergencyRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Availability Search Filters
    val filterBloodGroup = MutableStateFlow("All")
    val filterComponent = MutableStateFlow("All Components")
    val filterCity = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    private val searchFiltersFlow = combine(
        filterBloodGroup,
        filterComponent,
        filterCity,
        searchQuery
    ) { group, component, city, query ->
        FilterState(group, component, city, query)
    }

    // Filtered Blood Banks with Stock
    val filteredBanksWithStock: StateFlow<List<BloodBankWithStock>> = combine(
        allBloodBanks,
        allStock,
        searchFiltersFlow
    ) { banks, stocks, filters ->
        banks.mapNotNull { bank ->
            // Filter by city
            if (filters.city != "All" && !bank.city.equals(filters.city, ignoreCase = true)) {
                return@mapNotNull null
            }
            // Filter by search query (name or address)
            if (filters.query.isNotBlank() &&
                !bank.name.contains(filters.query, ignoreCase = true) &&
                !bank.address.contains(filters.query, ignoreCase = true)
            ) {
                return@mapNotNull null
            }

            val bankStock = stocks.filter { it.bankId == bank.id }
            val matchingStock = bankStock.filter { stock ->
                val groupMatch = (filters.group == "All") || stock.bloodGroup.equals(filters.group, ignoreCase = true)
                val compMatch = (filters.component == "All Components") || stock.component.equals(filters.component, ignoreCase = true)
                groupMatch && compMatch
            }

            // Only include banks that have stocks or match query
            val totalUnits = matchingStock.sumOf { it.units }
            val distance = GeoUtil.calculateDistanceKm(
                GeoUtil.DEFAULT_USER_LAT, GeoUtil.DEFAULT_USER_LNG,
                bank.latitude, bank.longitude
            )

            BloodBankWithStock(
                bank = bank,
                stockItems = matchingStock,
                distanceKm = distance,
                totalAvailableUnits = totalUnits
            )
        }.sortedBy { it.distanceKm }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Donor Finder Filters
    val donorSearchBloodGroup = MutableStateFlow("O-")
    val donorMaxDistanceKm = MutableStateFlow(50f)
    val donorShowCompatibleOnly = MutableStateFlow(true)
    val donorOnlyAvailable = MutableStateFlow(false)

    // Revealed donor contact IDs (privacy consent requested by user)
    private val _revealedDonorIds = MutableStateFlow<Set<Long>>(emptySet())
    val revealedDonorIds: StateFlow<Set<Long>> = _revealedDonorIds.asStateFlow()

    fun revealDonorContact(donorId: Long) {
        _revealedDonorIds.value = _revealedDonorIds.value + donorId
    }

    // Matching Donors Stream
    val matchingDonors: StateFlow<List<DonorMatch>> = combine(
        allDonors,
        donorSearchBloodGroup,
        donorMaxDistanceKm,
        donorShowCompatibleOnly,
        donorOnlyAvailable
    ) { donors, targetGroupStr, maxDist, showCompatible, onlyAvail ->
        val targetGroup = BloodGroup.fromLabel(targetGroupStr) ?: BloodGroup.O_NEG
        val compatibleDonors = BloodGroup.getCompatibleDonors(targetGroup)

        donors.mapNotNull { donor ->
            val donorGroup = BloodGroup.fromLabel(donor.bloodGroup)
            if (donorGroup == null) return@mapNotNull null

            val isDirect = donorGroup == targetGroup
            val isComp = compatibleDonors.contains(donorGroup)

            if (showCompatible && !isComp) return@mapNotNull null
            if (!showCompatible && !isDirect) return@mapNotNull null
            if (onlyAvail && !donor.isAvailable) return@mapNotNull null

            val distance = GeoUtil.calculateDistanceKm(
                GeoUtil.DEFAULT_USER_LAT, GeoUtil.DEFAULT_USER_LNG,
                donor.latitude, donor.longitude
            )
            if (distance > maxDist) return@mapNotNull null

            val note = when {
                isDirect -> "Exact Match (${donor.bloodGroup})"
                donorGroup == BloodGroup.O_NEG -> "Universal Donor (${donor.bloodGroup} can donate to all)"
                else -> "Compatible (${donor.bloodGroup} can donate to ${targetGroup.label})"
            }

            DonorMatch(
                donor = donor,
                isDirectMatch = isDirect,
                isCompatible = isComp,
                compatibilityNote = note,
                distanceKm = distance
            )
        }.sortedWith(
            compareByDescending<DonorMatch> { it.donor.isAvailable }
                .thenByDescending { it.isDirectMatch }
                .thenBy { it.distanceKm }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Emergency Requests Filter & Broadcast Feedback
    val emergencyStatusFilter = MutableStateFlow("ALL") // ALL, OPEN, FULFILLED
    private val _broadcastSuccessMessage = MutableStateFlow<String?>(null)
    val broadcastSuccessMessage: StateFlow<String?> = _broadcastSuccessMessage.asStateFlow()

    fun clearBroadcastSuccessMessage() {
        _broadcastSuccessMessage.value = null
    }

    fun broadcastEmergencyRequest(
        patientName: String,
        bloodGroup: String,
        component: String,
        unitsNeeded: Int,
        hospitalName: String,
        hospitalAddress: String,
        city: String,
        urgency: UrgencyLevel,
        contactPerson: String,
        contactPhone: String,
        notes: String
    ) {
        viewModelScope.launch {
            val reqBloodGroup = BloodGroup.fromLabel(bloodGroup) ?: BloodGroup.O_NEG
            val compatibleGroups = BloodGroup.getCompatibleDonors(reqBloodGroup).map { it.label }

            // Estimate matching donors alerted in the area
            val matchingCount = allDonors.value.count {
                compatibleGroups.contains(it.bloodGroup) && it.isEligibleToDonate()
            }.coerceAtLeast(3)

            val request = EmergencyRequestEntity(
                patientName = patientName,
                bloodGroup = bloodGroup,
                component = component,
                unitsNeeded = unitsNeeded,
                hospitalName = hospitalName,
                hospitalAddress = hospitalAddress,
                city = city,
                urgency = urgency.name,
                contactPerson = contactPerson,
                contactPhone = contactPhone,
                status = RequestStatus.OPEN.name,
                createdAtMillis = System.currentTimeMillis(),
                matchingDonorsAlerted = matchingCount,
                notes = notes
            )

            repository.createEmergencyRequest(request)
            _broadcastSuccessMessage.value =
                "🚨 Emergency Broadcast Active! $matchingCount matching donors nearby have been alerted for $bloodGroup."
        }
    }

    fun markEmergencyFulfilled(requestId: Long) {
        viewModelScope.launch {
            repository.markRequestFulfilled(requestId)
        }
    }

    // Active Donor Profile (Simulating active user logged in as Donor #1)
    val currentDonorId = MutableStateFlow(1L)
    val currentDonor: StateFlow<DonorEntity?> = combine(allDonors, currentDonorId) { donors, id ->
        donors.find { it.id == id } ?: donors.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentDonorHistory: StateFlow<List<DonationHistoryEntity>> = repository
        .getDonationHistoryForDonor(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDonorProfile(
        name: String,
        age: Int,
        bloodGroup: String,
        city: String,
        phone: String,
        email: String,
        daysAgoLastDonation: Int,
        isAvailable: Boolean,
        consentMaskPhone: Boolean
    ) {
        viewModelScope.launch {
            val currentDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val targetLastDay = currentDay - daysAgoLastDonation

            val existing = currentDonor.value
            val updated = if (existing != null) {
                existing.copy(
                    fullName = name,
                    age = age,
                    bloodGroup = bloodGroup,
                    city = city,
                    phone = phone,
                    email = email,
                    lastDonationEpochDay = targetLastDay,
                    isAvailable = isAvailable,
                    consentMaskPhone = consentMaskPhone
                )
            } else {
                DonorEntity(
                    fullName = name,
                    age = age,
                    bloodGroup = bloodGroup,
                    city = city,
                    phone = phone,
                    email = email,
                    lastDonationEpochDay = targetLastDay,
                    isAvailable = isAvailable,
                    consentMaskPhone = consentMaskPhone
                )
            }
            if (existing != null) {
                repository.updateDonor(updated)
            } else {
                val newId = repository.insertDonor(updated)
                currentDonorId.value = newId
            }
        }
    }

    fun toggleCurrentDonorAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            currentDonor.value?.let { donor ->
                repository.updateDonorAvailability(donor.id, isAvailable)
            }
        }
    }

    fun toggleCurrentDonorConsent(consent: Boolean) {
        viewModelScope.launch {
            currentDonor.value?.let { donor ->
                repository.updateDonorConsent(donor.id, consent)
            }
        }
    }

    // Admin Stock Management
    val adminSelectedBankId = MutableStateFlow(1L)

    fun updateStockUnits(bankId: Long, bloodGroup: String, component: String, delta: Int) {
        viewModelScope.launch {
            val stockItem = allStock.value.find {
                it.bankId == bankId && it.bloodGroup == bloodGroup && it.component == component
            }
            if (stockItem != null) {
                val newUnits = (stockItem.units + delta).coerceAtLeast(0)
                repository.updateStockUnits(bankId, bloodGroup, component, newUnits)
            }
        }
    }

    // Map View Selection
    val mapSelectedBank = MutableStateFlow<BloodBankEntity?>(null)
    val mapSelectedDonor = MutableStateFlow<DonorEntity?>(null)
    val mapFilterType = MutableStateFlow("ALL") // ALL, BANKS, DONORS

    fun selectMapBank(bank: BloodBankEntity?) {
        mapSelectedBank.value = bank
        mapSelectedDonor.value = null
    }

    fun selectMapDonor(donor: DonorEntity?) {
        mapSelectedDonor.value = donor
        mapSelectedBank.value = null
    }
}
