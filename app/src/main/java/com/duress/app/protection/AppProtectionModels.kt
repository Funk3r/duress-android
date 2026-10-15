package com.duress.app.protection

import android.content.Context
import androidx.annotation.StringRes
import com.duress.app.R

enum class ChecklistCategory {
    FINANCIAL_WALLET,
    BANKING,
    COMMUNICATION_MEDIA,
    SYSTEM_HARDENING
}

enum class ChecklistPriority {
    CRITICAL,
    HIGH,
    RECOMMENDED
}

enum class ChecklistActionType {
    LAUNCH_APP,
    OPEN_OEM_VAULT,
    OPEN_SECURITY_SETTINGS,
    OPEN_NOTIFICATION_SETTINGS,
    OPEN_SHELTER
}

data class AppChecklistItem(
    val id: String,
    @StringRes val titleRes: Int,
    val category: ChecklistCategory,
    val priority: ChecklistPriority,
    val packages: List<String>,
    val actionType: ChecklistActionType,
    @StringRes val descriptionRes: Int,
    @StringRes val stepByStepGuideRes: Int
)

data class OemGuideItem(
    val oemBrand: OemBrand,
    @StringRes val titleRes: Int,
    @StringRes val featureNameRes: Int,
    @StringRes val summaryRes: Int,
    @StringRes val stepsRes: Int,
    @StringRes val actionLabelRes: Int,
    val onAction: (Context) -> Unit
)

object AppProtectionData {

    val checklistItems = listOf(
        AppChecklistItem(
            id = "easypaisa",
            titleRes = R.string.check_easypaisa_title,
            category = ChecklistCategory.FINANCIAL_WALLET,
            priority = ChecklistPriority.CRITICAL,
            packages = listOf("pk.com.telenor.phoenix"),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_easypaisa_desc,
            stepByStepGuideRes = R.string.check_easypaisa_steps
        ),
        AppChecklistItem(
            id = "jazzcash",
            titleRes = R.string.check_jazzcash_title,
            category = ChecklistCategory.FINANCIAL_WALLET,
            priority = ChecklistPriority.CRITICAL,
            packages = listOf("com.techlogix.mobilinkcustomer"),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_jazzcash_desc,
            stepByStepGuideRes = R.string.check_jazzcash_steps
        ),
        AppChecklistItem(
            id = "sadapay",
            titleRes = R.string.check_sadapay_title,
            category = ChecklistCategory.FINANCIAL_WALLET,
            priority = ChecklistPriority.HIGH,
            packages = listOf("pk.sadapay.app"),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_sadapay_desc,
            stepByStepGuideRes = R.string.check_sadapay_steps
        ),
        AppChecklistItem(
            id = "nayapay",
            titleRes = R.string.check_nayapay_title,
            category = ChecklistCategory.FINANCIAL_WALLET,
            priority = ChecklistPriority.HIGH,
            packages = listOf("com.nayapay.wallet"),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_nayapay_desc,
            stepByStepGuideRes = R.string.check_nayapay_steps
        ),
        AppChecklistItem(
            id = "commercial_banks",
            titleRes = R.string.check_banks_title,
            category = ChecklistCategory.BANKING,
            priority = ChecklistPriority.CRITICAL,
            packages = listOf(
                "com.hbl.mobilebanking",
                "com.divergentsoftlabs.meezanbank",
                "com.mcb.mcblive",
                "com.ubl.digital",
                "com.alfa.mobile"
            ),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_banks_desc,
            stepByStepGuideRes = R.string.check_banks_steps
        ),
        AppChecklistItem(
            id = "whatsapp",
            titleRes = R.string.check_whatsapp_title,
            category = ChecklistCategory.COMMUNICATION_MEDIA,
            priority = ChecklistPriority.HIGH,
            packages = listOf("com.whatsapp"),
            actionType = ChecklistActionType.LAUNCH_APP,
            descriptionRes = R.string.check_whatsapp_desc,
            stepByStepGuideRes = R.string.check_whatsapp_steps
        ),
        AppChecklistItem(
            id = "gallery_cnic",
            titleRes = R.string.check_gallery_title,
            category = ChecklistCategory.COMMUNICATION_MEDIA,
            priority = ChecklistPriority.HIGH,
            packages = listOf("com.google.android.apps.photos"),
            actionType = ChecklistActionType.OPEN_OEM_VAULT,
            descriptionRes = R.string.check_gallery_desc,
            stepByStepGuideRes = R.string.check_gallery_steps
        ),
        AppChecklistItem(
            id = "sim_card_pin",
            titleRes = R.string.check_sim_pin_title,
            category = ChecklistCategory.SYSTEM_HARDENING,
            priority = ChecklistPriority.CRITICAL,
            packages = emptyList(),
            actionType = ChecklistActionType.OPEN_SECURITY_SETTINGS,
            descriptionRes = R.string.check_sim_pin_desc,
            stepByStepGuideRes = R.string.check_sim_pin_steps
        ),
        AppChecklistItem(
            id = "lockscreen_notifications",
            titleRes = R.string.check_lockscreen_notif_title,
            category = ChecklistCategory.SYSTEM_HARDENING,
            priority = ChecklistPriority.HIGH,
            packages = emptyList(),
            actionType = ChecklistActionType.OPEN_NOTIFICATION_SETTINGS,
            descriptionRes = R.string.check_lockscreen_notif_desc,
            stepByStepGuideRes = R.string.check_lockscreen_notif_steps
        ),
        AppChecklistItem(
            id = "device_passcode",
            titleRes = R.string.check_device_passcode_title,
            category = ChecklistCategory.SYSTEM_HARDENING,
            priority = ChecklistPriority.CRITICAL,
            packages = emptyList(),
            actionType = ChecklistActionType.OPEN_SECURITY_SETTINGS,
            descriptionRes = R.string.check_device_passcode_desc,
            stepByStepGuideRes = R.string.check_device_passcode_steps
        )
    )

    fun getOemGuides(): List<OemGuideItem> = listOf(
        OemGuideItem(
            oemBrand = OemBrand.TRANSSION,
            titleRes = R.string.guide_transsion_title,
            featureNameRes = R.string.guide_transsion_feature,
            summaryRes = R.string.guide_transsion_summary,
            stepsRes = R.string.guide_transsion_steps,
            actionLabelRes = R.string.guide_transsion_btn,
            onAction = { AppProtectionHelper.openTranssionXHide(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.SAMSUNG,
            titleRes = R.string.guide_samsung_title,
            featureNameRes = R.string.guide_samsung_feature,
            summaryRes = R.string.guide_samsung_summary,
            stepsRes = R.string.guide_samsung_steps,
            actionLabelRes = R.string.guide_samsung_btn,
            onAction = { AppProtectionHelper.openSamsungSecureFolder(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.XIAOMI,
            titleRes = R.string.guide_xiaomi_title,
            featureNameRes = R.string.guide_xiaomi_feature,
            summaryRes = R.string.guide_xiaomi_summary,
            stepsRes = R.string.guide_xiaomi_steps,
            actionLabelRes = R.string.guide_xiaomi_btn,
            onAction = { AppProtectionHelper.openXiaomiSecondSpace(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.VIVO,
            titleRes = R.string.guide_vivo_title,
            featureNameRes = R.string.guide_vivo_feature,
            summaryRes = R.string.guide_vivo_summary,
            stepsRes = R.string.guide_vivo_steps,
            actionLabelRes = R.string.guide_vivo_btn,
            onAction = { AppProtectionHelper.openVivoAppHide(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.OPPO,
            titleRes = R.string.guide_oppo_title,
            featureNameRes = R.string.guide_oppo_feature,
            summaryRes = R.string.guide_oppo_summary,
            stepsRes = R.string.guide_oppo_steps,
            actionLabelRes = R.string.guide_oppo_btn,
            onAction = { AppProtectionHelper.openOppoPrivateSafe(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.GOOGLE,
            titleRes = R.string.guide_private_space_title,
            featureNameRes = R.string.guide_private_space_feature,
            summaryRes = R.string.guide_private_space_summary,
            stepsRes = R.string.guide_private_space_steps,
            actionLabelRes = R.string.guide_private_space_btn,
            onAction = { AppProtectionHelper.openPrivateSpaceSettings(it) }
        ),
        OemGuideItem(
            oemBrand = OemBrand.GENERIC,
            titleRes = R.string.guide_shelter_title,
            featureNameRes = R.string.guide_shelter_feature,
            summaryRes = R.string.guide_shelter_summary,
            stepsRes = R.string.guide_shelter_steps,
            actionLabelRes = R.string.guide_shelter_btn,
            onAction = { AppProtectionHelper.openShelterOrIsland(it) }
        )
    )
}
