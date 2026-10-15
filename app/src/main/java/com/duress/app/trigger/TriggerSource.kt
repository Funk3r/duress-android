package com.duress.app.trigger

enum class TriggerSource(val id: String, val displayNameEn: String, val displayNameUr: String) {
    KEY_SEQUENCE("KEY_SEQUENCE", "Hardware Key Sequence", "ہارڈویئر بٹن کی ترتیب"),
    QS_TILE("QS_TILE", "Quick Settings Tile", "کوئیک سیٹنگز ٹائل"),
    WIDGET("WIDGET", "Home Screen Widget", "ہوم اسکرین وجٹ"),
    DECOY_PIN("DECOY_PIN", "Duress Decoy PIN", "خفیہ دھوکہ دہی پن"),
    TEST_BUTTON("TEST_BUTTON", "Test Button (In-App)", "ٹیسٹ بٹن (ایپ کے اندر)"),
    SYSTEM_BOOT("SYSTEM_BOOT", "System Boot Restore", "سسٹم ری بوٹ بحالی");

    companion object {
        fun fromId(id: String): TriggerSource {
            return entries.firstOrNull { it.id == id } ?: KEY_SEQUENCE
        }
    }
}
