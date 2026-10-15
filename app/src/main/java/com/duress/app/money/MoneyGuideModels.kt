package com.duress.app.money

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import com.duress.app.R

enum class MoneyPillarCategory {
    TRANSACTION_LIMITS,
    BENEFICIARY_WHITELIST,
    TIME_LOCK_SAVINGS,
    HARDWARE_WALLET,
    REMOTE_LOCK
}

enum class HelplineCategory {
    TELCO_SIM,
    WALLET_EMI,
    COMMERCIAL_BANK
}

data class MoneyPillarItem(
    val id: String,
    val category: MoneyPillarCategory,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val whyRes: Int,
    @StringRes val stepsRes: Int,
    val isCritical: Boolean = true
)

data class EmergencyHelpline(
    val id: String,
    val name: String,
    val dialNumber: String,
    val displayPhone: String,
    val category: HelplineCategory,
    @StringRes val notesRes: Int
)

object MoneyGuideData {

    val pillars = listOf(
        MoneyPillarItem(
            id = "limits",
            category = MoneyPillarCategory.TRANSACTION_LIMITS,
            titleRes = R.string.pillar_limits_title,
            subtitleRes = R.string.pillar_limits_subtitle,
            whyRes = R.string.pillar_limits_why,
            stepsRes = R.string.pillar_limits_steps,
            isCritical = true
        ),
        MoneyPillarItem(
            id = "whitelist",
            category = MoneyPillarCategory.BENEFICIARY_WHITELIST,
            titleRes = R.string.pillar_whitelist_title,
            subtitleRes = R.string.pillar_whitelist_subtitle,
            whyRes = R.string.pillar_whitelist_why,
            stepsRes = R.string.pillar_whitelist_steps,
            isCritical = true
        ),
        MoneyPillarItem(
            id = "timelock",
            category = MoneyPillarCategory.TIME_LOCK_SAVINGS,
            titleRes = R.string.pillar_timelock_title,
            subtitleRes = R.string.pillar_timelock_subtitle,
            whyRes = R.string.pillar_timelock_why,
            stepsRes = R.string.pillar_timelock_steps,
            isCritical = true
        ),
        MoneyPillarItem(
            id = "hardware_wallet",
            category = MoneyPillarCategory.HARDWARE_WALLET,
            titleRes = R.string.pillar_crypto_title,
            subtitleRes = R.string.pillar_crypto_subtitle,
            whyRes = R.string.pillar_crypto_why,
            stepsRes = R.string.pillar_crypto_steps,
            isCritical = false
        ),
        MoneyPillarItem(
            id = "remote_lock",
            category = MoneyPillarCategory.REMOTE_LOCK,
            titleRes = R.string.pillar_remote_title,
            subtitleRes = R.string.pillar_remote_subtitle,
            whyRes = R.string.pillar_remote_why,
            stepsRes = R.string.pillar_remote_steps,
            isCritical = true
        )
    )

    val helplines = listOf(
        // Telco SIM Blocking
        EmergencyHelpline("jazz", "Jazz / Warid", "111300300", "111-300-300 (or 111 from Jazz)", HelplineCategory.TELCO_SIM, R.string.helpline_sim_block_note),
        EmergencyHelpline("telenor", "Telenor Pakistan", "042111345962", "345 (or 042-111-345-962)", HelplineCategory.TELCO_SIM, R.string.helpline_sim_block_note),
        EmergencyHelpline("zong", "Zong 4G (CMPak)", "051111222111", "310 (or 051-111-222-111)", HelplineCategory.TELCO_SIM, R.string.helpline_sim_block_note),
        EmergencyHelpline("ufone", "Ufone 4G (PTCL)", "03311333100", "333 (or 033-11-333-100)", HelplineCategory.TELCO_SIM, R.string.helpline_sim_block_note),

        // Wallets & EMIs
        EmergencyHelpline("easypaisa", "Easypaisa (Telenor Bank)", "042111003737", "3737 (or 042-111-003-737)", HelplineCategory.WALLET_EMI, R.string.helpline_wallet_block_note),
        EmergencyHelpline("jazzcash", "JazzCash (Mobilink Bank)", "021111124444", "4444 (or 021-111-124-444)", HelplineCategory.WALLET_EMI, R.string.helpline_wallet_block_note),
        EmergencyHelpline("sadapay", "SadaPay EMI", "080017232", "0800-1-SADA (0800-17232)", HelplineCategory.WALLET_EMI, R.string.helpline_wallet_block_note),
        EmergencyHelpline("nayapay", "NayaPay EMI", "021111222729", "021-111-222-729", HelplineCategory.WALLET_EMI, R.string.helpline_wallet_block_note),

        // Commercial Banks
        EmergencyHelpline("hbl", "Habib Bank Limited (HBL)", "021111111425", "021-111-111-425", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("meezan", "Meezan Bank (Islamic)", "021111331331", "021-111-331-331", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("mcb", "MCB Bank Live", "021111000622", "021-111-000-622", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("ubl", "United Bank Limited (UBL)", "021111825888", "021-111-825-888", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("alfalah", "Bank Alfalah", "021111225111", "021-111-225-111", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("abl", "Allied Bank (ABL)", "021111225225", "021-111-225-225", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("faysal", "Faysal Bank", "021111060606", "021-111-060-606", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note),
        EmergencyHelpline("scb", "Standard Chartered Pakistan", "021111002002", "021-111-002-002", HelplineCategory.COMMERCIAL_BANK, R.string.helpline_bank_block_note)
    )

    fun dialNumber(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open dialer for $number", Toast.LENGTH_SHORT).show()
        }
    }
}
