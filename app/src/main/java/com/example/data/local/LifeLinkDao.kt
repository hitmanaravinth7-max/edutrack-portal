package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BloodBankEntity
import com.example.data.model.BloodStockEntity
import com.example.data.model.DonationHistoryEntity
import com.example.data.model.DonorEntity
import com.example.data.model.EmergencyRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeLinkDao {
    // Blood Banks
    @Query("SELECT * FROM blood_banks ORDER BY name ASC")
    fun getAllBloodBanks(): Flow<List<BloodBankEntity>>

    @Query("SELECT * FROM blood_banks WHERE id = :id LIMIT 1")
    fun getBloodBankById(id: Long): Flow<BloodBankEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodBank(bank: BloodBankEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBloodBanks(banks: List<BloodBankEntity>)

    // Blood Stock
    @Query("SELECT * FROM blood_stock")
    fun getAllStock(): Flow<List<BloodStockEntity>>

    @Query("SELECT * FROM blood_stock WHERE bankId = :bankId")
    fun getStockForBank(bankId: Long): Flow<List<BloodStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: BloodStockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStock(stocks: List<BloodStockEntity>)

    @Query("UPDATE blood_stock SET units = :units, lastUpdatedMillis = :lastUpdated WHERE bankId = :bankId AND bloodGroup = :bloodGroup AND component = :component")
    suspend fun updateStockUnits(bankId: Long, bloodGroup: String, component: String, units: Int, lastUpdated: Long = System.currentTimeMillis())

    // Donors
    @Query("SELECT * FROM donors ORDER BY fullName ASC")
    fun getAllDonors(): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE id = :id LIMIT 1")
    fun getDonorById(id: Long): Flow<DonorEntity?>

    @Query("SELECT * FROM donors WHERE email = :email LIMIT 1")
    suspend fun getDonorByEmail(email: String): DonorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDonors(donors: List<DonorEntity>)

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Query("UPDATE donors SET isAvailable = :isAvailable WHERE id = :donorId")
    suspend fun updateDonorAvailability(donorId: Long, isAvailable: Boolean)

    @Query("UPDATE donors SET consentMaskPhone = :consent WHERE id = :donorId")
    suspend fun updateDonorConsent(donorId: Long, consent: Boolean)

    // Emergency Requests
    @Query("SELECT * FROM emergency_requests ORDER BY createdAtMillis DESC")
    fun getAllEmergencyRequests(): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM emergency_requests WHERE id = :id LIMIT 1")
    fun getEmergencyRequestById(id: Long): Flow<EmergencyRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyRequest(request: EmergencyRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmergencyRequests(requests: List<EmergencyRequestEntity>)

    @Query("UPDATE emergency_requests SET status = :status WHERE id = :id")
    suspend fun updateEmergencyRequestStatus(id: Long, status: String)

    // Donation History
    @Query("SELECT * FROM donation_history WHERE donorId = :donorId ORDER BY dateEpochMillis DESC")
    fun getDonationHistoryForDonor(donorId: Long): Flow<List<DonationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationHistory(history: DonationHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDonationHistory(history: List<DonationHistoryEntity>)
}
