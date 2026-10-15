Project: "Duress", an Android personal-safety app for users forced to hand over their phone. Target: Pakistan, budget phones (Infinix, Tecno, Redmi, Vivo, Oppo, Samsung A), Android 8-15, sideloaded.



Stack: Kotlin, Compose M3, DataStore, minSdk 26, single module. No network, analytics, root, Shizuku or ADB. English + Urdu (RTL).



Rules:

\- Every action is failure-isolated and toggleable.

\- Request each permission with an explanation first (Accessibility, Location, SMS, Notifications, DND policy access, Android 13+ restricted settings guide).

\- API 28+: GLOBAL\_ACTION\_LOCK\_SCREEN. API 26-27 or if unavailable: DevicePolicyManager.lockNow via optional device admin.

\- Log which trigger path fired (key sequence, tile, widget), on-device only.

\- The app cannot uninstall other apps. Never claim it can.

\- Work one phase at a time. After each: build, fix errors, list manifest permissions added, give a 5-line device test checklist. Wait for "next".



Phases:

1 Trigger: AccessibilityService key sequence (default Vol Down x4 in 3s, configurable), haptic confirm, debounce, QS tile, widget, optional stealth icon/label.

2 Pipeline: silent SMS SOS with maps link (refresh 60s for 15 min), lock device, DND on, clear own logs, Test mode (SMS to self only).

3 Decoy: real PIN vs duress PIN (empty-looking app + silent pipeline), Keystore encryption.

4 Protect apps: guides and deep links for Private Space (Android 15+), OEM hidden apps/second space/secure folder, Shelter/Island fallback, per-app PIN checklist.

5 Money guide: offline Urdu/English checklist (limits, whitelist, time-lock, hardware wallet, remote lock).

6 Hardening: boot restore, Doze, per-brand keep-alive wizard, accidental-trigger guards, privacy screen.

