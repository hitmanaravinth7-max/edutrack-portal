package com.example

import com.example.data.model.BloodGroup
import com.example.data.model.DonorEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ExampleUnitTest {
  @Test
  fun testUniversalDonorCompatibility() {
    val recipients = BloodGroup.getCompatibleRecipients(BloodGroup.O_NEG)
    assertEquals(8, recipients.size)
    assertTrue(recipients.contains(BloodGroup.AB_POS))
    assertTrue(recipients.contains(BloodGroup.O_POS))
  }

  @Test
  fun testUniversalRecipientCompatibility() {
    val donors = BloodGroup.getCompatibleDonors(BloodGroup.AB_POS)
    assertEquals(8, donors.size)
  }

  @Test
  fun testSpecificCompatibility() {
    // Recipient A+ can receive from O-, O+, A-, A+
    val aPosDonors = BloodGroup.getCompatibleDonors(BloodGroup.A_POS)
    assertEquals(4, aPosDonors.size)
    assertTrue(aPosDonors.contains(BloodGroup.O_NEG))
    assertTrue(aPosDonors.contains(BloodGroup.A_POS))
    assertFalse(aPosDonors.contains(BloodGroup.B_POS))
  }

  @Test
  fun testDonorEligibilityCooldown() {
    val currentDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    
    // Donated 100 days ago -> Eligible
    val eligibleDonor = DonorEntity(
      fullName = "Test Eligible",
      age = 30,
      bloodGroup = "O+",
      city = "Metro",
      phone = "555-1234",
      email = "test@test.com",
      lastDonationEpochDay = currentDay - 100
    )
    assertTrue(eligibleDonor.isEligibleToDonate())
    assertEquals(0, eligibleDonor.daysUntilEligible())

    // Donated 40 days ago -> Ineligible (50 days remaining)
    val coolingDonor = DonorEntity(
      fullName = "Test Ineligible",
      age = 30,
      bloodGroup = "O+",
      city = "Metro",
      phone = "555-1234",
      email = "test@test.com",
      lastDonationEpochDay = currentDay - 40
    )
    assertFalse(coolingDonor.isEligibleToDonate())
    assertEquals(50, coolingDonor.daysUntilEligible())
  }
}
