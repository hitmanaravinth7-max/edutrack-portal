package com.example.data.model

enum class BloodGroup(val label: String) {
    O_NEG("O-"),
    O_POS("O+"),
    A_NEG("A-"),
    A_POS("A+"),
    B_NEG("B-"),
    B_POS("B+"),
    AB_NEG("AB-"),
    AB_POS("AB+");

    companion object {
        fun fromLabel(label: String): BloodGroup? =
            entries.find { it.label.equals(label.trim(), ignoreCase = true) }

        /**
         * Returns list of blood groups that can DONATE to [recipientGroup].
         */
        fun getCompatibleDonors(recipientGroup: BloodGroup): List<BloodGroup> {
            return when (recipientGroup) {
                O_NEG -> listOf(O_NEG)
                O_POS -> listOf(O_NEG, O_POS)
                A_NEG -> listOf(O_NEG, A_NEG)
                A_POS -> listOf(O_NEG, O_POS, A_NEG, A_POS)
                B_NEG -> listOf(O_NEG, B_NEG)
                B_POS -> listOf(O_NEG, O_POS, B_NEG, B_POS)
                AB_NEG -> listOf(O_NEG, A_NEG, B_NEG, AB_NEG)
                AB_POS -> listOf(O_NEG, O_POS, A_NEG, A_POS, B_NEG, B_POS, AB_NEG, AB_POS)
            }
        }

        /**
         * Returns list of blood groups that [donorGroup] can donate TO.
         */
        fun getCompatibleRecipients(donorGroup: BloodGroup): List<BloodGroup> {
            return when (donorGroup) {
                O_NEG -> listOf(O_NEG, O_POS, A_NEG, A_POS, B_NEG, B_POS, AB_NEG, AB_POS)
                O_POS -> listOf(O_POS, A_POS, B_POS, AB_POS)
                A_NEG -> listOf(A_NEG, A_POS, AB_NEG, AB_POS)
                A_POS -> listOf(A_POS, AB_POS)
                B_NEG -> listOf(B_NEG, B_POS, AB_NEG, AB_POS)
                B_POS -> listOf(B_POS, AB_POS)
                AB_NEG -> listOf(AB_NEG, AB_POS)
                AB_POS -> listOf(AB_POS)
            }
        }

        fun isCompatible(donor: BloodGroup, recipient: BloodGroup): Boolean {
            return getCompatibleDonors(recipient).contains(donor)
        }
    }
}

enum class BloodComponent(val displayName: String, val shelfLifeDays: Int) {
    ALL("All Components", 0),
    WHOLE_BLOOD("Whole Blood", 35),
    PLASMA("Plasma", 365),
    PLATELETS("Platelets", 5),
    RBC("Red Blood Cells", 42);

    companion object {
        fun fromDisplayName(name: String): BloodComponent =
            entries.find { it.displayName.equals(name.trim(), ignoreCase = true) } ?: WHOLE_BLOOD
    }
}

enum class UrgencyLevel(val displayName: String, val levelPriority: Int) {
    CRITICAL("Critical (Immediate)", 1),
    WITHIN_24H("Within 24 Hours", 2),
    PLANNED("Planned / Scheduled", 3);

    companion object {
        fun fromString(str: String): UrgencyLevel =
            entries.find { it.name.equals(str.trim(), ignoreCase = true) } ?: CRITICAL
    }
}

enum class RequestStatus {
    OPEN,
    FULFILLED;

    companion object {
        fun fromString(str: String): RequestStatus =
            entries.find { it.name.equals(str.trim(), ignoreCase = true) } ?: OPEN
    }
}
