package com.example.data.local

import com.example.data.model.BloodBankEntity
import com.example.data.model.BloodStockEntity
import com.example.data.model.DonationHistoryEntity
import com.example.data.model.DonorEntity
import com.example.data.model.EmergencyRequestEntity
import kotlinx.coroutines.flow.Flow

class LifeLinkRepository(private val dao: LifeLinkDao) {

    val allBloodBanks: Flow<List<BloodBankEntity>> = dao.getAllBloodBanks()
    val allStock: Flow<List<BloodStockEntity>> = dao.getAllStock()
    val allDonors: Flow<List<DonorEntity>> = dao.getAllDonors()
    val allEmergencyRequests: Flow<List<EmergencyRequestEntity>> = dao.getAllEmergencyRequests()

    fun getStockForBank(bankId: Long): Flow<List<BloodStockEntity>> = dao.getStockForBank(bankId)

    fun getDonorById(id: Long): Flow<DonorEntity?> = dao.getDonorById(id)

    fun getDonationHistoryForDonor(donorId: Long): Flow<List<DonationHistoryEntity>> =
        dao.getDonationHistoryForDonor(donorId)

    suspend fun insertBloodBank(bank: BloodBankEntity): Long = dao.insertBloodBank(bank)

    suspend fun updateStockUnits(
        bankId: Long,
        bloodGroup: String,
        component: String,
        units: Int
    ) = dao.updateStockUnits(bankId, bloodGroup, component, units)

    suspend fun insertDonor(donor: DonorEntity): Long = dao.insertDonor(donor)

    suspend fun updateDonor(donor: DonorEntity) = dao.updateDonor(donor)

    suspend fun updateDonorAvailability(donorId: Long, isAvailable: Boolean) =
        dao.updateDonorAvailability(donorId, isAvailable)

    suspend fun updateDonorConsent(donorId: Long, consent: Boolean) =
        dao.updateDonorConsent(donorId, consent)

    suspend fun createEmergencyRequest(request: EmergencyRequestEntity): Long =
        dao.insertEmergencyRequest(request)

    suspend fun markRequestFulfilled(requestId: Long) =
        dao.updateEmergencyRequestStatus(requestId, "FULFILLED")

    suspend fun recordDonation(history: DonationHistoryEntity): Long =
        dao.insertDonationHistory(history)
}
