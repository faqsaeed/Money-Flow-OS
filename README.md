# MONEY FLOW OS (Offline-First, Deterministic)

MONEY FLOW OS is a production-grade, offline-first Android personal finance intelligence system.

- Reads incoming SMS bank/wallet notifications locally on-device
- Parses transactions using **regex + deterministic rules only**
- Stores immutable raw SMS + structured transactions in **Room (SQLite)**
- Provides a modern fintech dashboard + people relationship ledger
- Includes a **PIN-protected Admin Mode** with full correction + audit trail

**No AI / ML / LLM features exist or are permitted in this codebase.**

## Requirements

- Android Studio (latest stable)
- Android SDK (compileSdk 36 / targetSdk 36)
- minSdk 26 (Android 8.0)

## Project modules (Clean Architecture)

- `app`: App shell, navigation, DI, theme
- `sms`: SMS receiver + WorkManager ingestion worker
- `core:domain`: Domain models + repository interfaces
- `core:parser`: Deterministic regex/rule engine + ingestion filter
- `core:database`: Room schema + DAO + repository implementations
- `core:security`: Admin PIN hashing + encrypted storage + admin session
- `feature:*`: Compose UI feature screens
  - `feature:dashboard`, `feature:transactions`, `feature:people`, `feature:session`, `feature:admin`

## SMS ingestion rules (as implemented)

Only processes SMS if:

- sender digits == `8558` (trusted JazzCash source), OR
- message contains any keyword (case-insensitive): `sent`, `received`, `IBFT`, `RAAST`, `POS`, `ATM`, `Mobile Load`, `loan`, `credit`

All other SMS are ignored.

## Permissions

This app requests:

- `RECEIVE_SMS` (required to ingest incoming bank/wallet SMS automatically)
- `READ_SMS` (used for future: manual import/backfill; still requested to keep setup simple)

Note: SMS permissions are sensitive and may require special handling for Play Store distribution. This repo targets on-device/offline usage.

## Admin Mode (PIN + optional biometrics)

- Hidden entry: open `Session` tab → **long-press** the "Admin Panel" card
- If PIN isn’t set yet, you’ll be prompted to create one (4–8 digits)
- Biometric unlock is offered when available (`BIOMETRIC_STRONG`)

Admin can:

- Edit any transaction (amount, direction, channel, category, person)
- Bulk edit overlays across multiple transactions
- View correction logs (full audit trail)
- Trigger recalculation (rebuild people ledger deterministically)
- Reset session (archive + start new)

## Data integrity rules

- Raw SMS (`raw_sms`) is **never deleted**
- Parsed fields are never overwritten; corrections are stored as **overlays**
- Every correction writes to `correction_log` with old/new JSON + reason + timestamp + admin_id

## Sample SMS dataset

See:

- `sample/sms_samples.md`
- `sample/parsed_outputs.json`

## Adding new parser rules (deterministic)

- Add a new `SmsParserRule` in `core/parser/src/main/java/com/moneyflowos/core/parser/rules/GenericRules.kt`
- Keep rules conservative: if you can’t reliably extract **amount + direction**, return `null`
- Add a unit test in `core/parser/src/test/java/...`

## Notes

- All analytics are computed locally from Room queries using effective (corrected) fields.
- Relationship graph is deterministic and offline (no external libs, no network).

## Running Money Flow OS on a Real Phone

This section is written for non-technical users.

### What you need

- An Android phone (Android 8.0+)
- A computer with Android Studio installed (Windows/Mac/Linux)
- A USB cable (or any way to copy a file to your phone)

### Step 1 — Build the app (Debug APK)

On your computer, open this project in Android Studio.

Then build a debug APK using **one** of these options:

- Android Studio: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
- Command line (recommended): open a Terminal in the project folder and run:
  - `./gradlew :app:assembleDebug`

### Step 2 — Find the APK file

After building, the APK will be here:

- `app/build/outputs/apk/debug/app-debug.apk`

### Step 3 — Transfer the APK to your phone

Use any method you prefer:

- USB cable: copy `app-debug.apk` to `Downloads/` on your phone
- Send it to yourself (WhatsApp/Telegram/Email) and download it on the phone

### Step 4 — Allow installing apps from “Unknown sources”

On your phone:

1. Open `Settings`
2. Go to `Security` (or `Privacy`)
3. Find `Install unknown apps` / `Unknown sources`
4. Choose the app you will use to open the APK (usually `Files` / `Chrome`)
5. Turn on **Allow from this source**

### Step 5 — Install the APK safely

1. Open the `Files` app on your phone
2. Go to `Downloads`
3. Tap `app-debug.apk`
4. Tap `Install`

If Android warns you, confirm only if you trust the APK you built yourself.

### Step 6 — Grant SMS permissions

On first launch, the app will ask for SMS access.

Tap **Grant SMS Access**, then allow:

- Receive SMS
- Read SMS

If you accidentally denied it:

1. Open `Settings` → `Apps` → `MONEY FLOW OS`
2. Tap `Permissions`
3. Allow `SMS`

### Step 7 — Test with a real JazzCash SMS (sender `8558`)

To test ingestion:

1. Keep MONEY FLOW OS installed and opened at least once
2. Make sure `Session` is **ACTIVE** (open the `Session` tab)
3. Trigger a real JazzCash notification SMS (sender `8558`) by doing a small transaction
4. Open the app:
   - `Transactions` should show the new entry
   - `Dashboard` totals should update
   - `People` should show the counterparty (if parsed)

### Step 8 — Open Admin Mode (PIN protected)

1. Open the `Session` tab
2. Long-press the **Admin Panel** card
3. Set a PIN (first time) or enter your PIN
4. Use Admin Mode to correct parsing mistakes and view the audit log

