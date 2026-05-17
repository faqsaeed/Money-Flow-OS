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

