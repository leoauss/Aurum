# UPI QR Scan-and-Pay — Implementation Plan

> Derived from [architecutre_report.md](file:///d:/Projects/MyApplication/architecutre_report.md)

---

## Step 1 — Project Bootstrapping & Configuration

Set up the Android project skeleton, dependency graph, and build configuration before writing any feature code.

### 1.1 Create the Android project
- Create a new Android project with **Kotlin** and **Jetpack Compose** template (Empty Compose Activity).
- Set **`minSdk = 26`**, **`targetSdk`** to the latest SDK required by Play.
- Package name: choose a reverse-domain name (e.g. `com.yourname.upipay`).

### 1.2 Configure Gradle dependencies
Add all required libraries to `build.gradle.kts` (app-level):

| Category | Libraries |
|---|---|
| Compose & Material 3 | `androidx.compose.material3`, `androidx.navigation.compose` |
| CameraX | `camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view` |
| ML Kit Barcode | `com.google.mlkit:barcode-scanning` (bundled) |
| Hilt | `hilt-android`, `hilt-compiler`, `hilt-navigation-compose` |
| Room | `room-runtime`, `room-ktx`, `room-compiler` |
| DataStore | `androidx.datastore:datastore-preferences` |
| Testing | `junit`, `turbine`, `compose-ui-test`, `espresso-intents`, `hilt-testing` |

### 1.3 Create the package structure
```
app/src/main/java/com/<pkg>/
  ui/
    scan/
    confirm/
    amount/
    picker/
    result/
    history/
    navigation/
    theme/
  domain/
    model/
    usecase/
  data/
    upi/
    db/
    prefs/
  di/
```

### 1.4 Set up the app theme ✅

#### 1.4.1 Create `Color.kt` ✅
- File: `ui/theme/Color.kt`
- Defined semantic `ColorTokens` data class with 15 tokens.
- Defined `DarkColorTokens` (primary) and `LightColorTokens` with exact HSL trailing comments and contrast verifications.


#### 1.4.2 Create `Type.kt` ✅

- **1.4.2.1 Add Google Fonts dependency** ✅
  - Added `androidx-ui-text-google-fonts` to `gradle/libs.versions.toml`.
  - Added `implementation(libs.androidx.ui.text.google.fonts)` to `app/build.gradle.kts`.

- **1.4.2.2 Configure GoogleFont Provider & Outfit FontFamily** ✅
  - Created `font_certs.xml` with Google Play Services certificates.
  - Configured `GoogleFont.Provider` pointing to `com.google.android.gms.fonts`.
  - Defined `OutfitFontFamily` with weights 400 (Normal), 500 (Medium), 600 (SemiBold), 700 (Bold).

- **1.4.2.3 Define Material 3 Typography** ✅
  - Configured complete Material 3 `Typography` scale (`displayLarge` through `labelSmall`) with the Outfit font family.

- **1.4.2.4 Verify build** ✅
  - Verified compilation via `:app:compileDebugKotlin` (BUILD SUCCESSFUL).



#### 1.4.3 Create `Shape.kt` ✅

- **1.4.3.1 Define corner radius tokens & semantics** ✅
  - Mapped corner radii to Material 3 UI roles:
    - `extraSmall = 4.dp` (badges, chips)
    - `small = 8.dp` (buttons, input fields)
    - `medium = 16.dp` (cards, dialogs, keypad surfaces)
    - `large = 24.dp` (bottom sheets, primary action containers)
    - `extraLarge = 32.dp` (modal bottom sheet corners, prominent modals)

- **1.4.3.2 Implement `Shape.kt`** ✅
  - Created `ui/theme/Shape.kt` defining `AppShapes` (and `Shapes` alias) using `RoundedCornerShape`.

- **1.4.3.3 Verify compilation** ✅
  - Verified `:app:compileDebugKotlin` compiles cleanly with zero errors (`BUILD SUCCESSFUL`).



#### 1.4.4 Create `Theme.kt` ✅

- **1.4.4.1 Map `ColorTokens` to Material 3 `ColorScheme`** ✅
  - Mapped `DarkColorTokens` to full `darkColorScheme`.
  - Mapped `LightColorTokens` to full `lightColorScheme`.
  - Provided `LocalColorTokens` to expose custom tokens (`success`, `warning`, `surfaceRaised`, `onSurfaceMuted`).

- **1.4.4.2 Implement `AurumTheme` Composable** ✅
  - Configured dynamic color on API 31+ (`dynamicDarkColorScheme` / `dynamicLightColorScheme`).
  - Added fallback to static `ColorTokens` scheme.
  - Wired `AppTypography` (from `Type.kt`) and `AppShapes` (from `Shape.kt`) into `MaterialTheme`.
  - Provided helper `AurumTheme.colors` accessor.

- **1.4.4.3 Ensure Edge-to-Edge display in `MainActivity.kt`** ✅
  - Configured `WindowCompat.setDecorFitsSystemWindows(window, false)` and `enableEdgeToEdge()`.

- **1.4.4.4 Verify compilation & build** ✅
  - Verified `:app:compileDebugKotlin` and full `:app:assembleDebug` compile and package cleanly (`BUILD SUCCESSFUL in 1m 5s`).



### 1.5 Configure the AndroidManifest ✅

- **1.5.1 Permissions & Features** ✅
  - Added `<uses-permission android:name="android.permission.CAMERA" />`.
  - Added `<uses-feature android:name="android.hardware.camera" android:required="false" />`.

- **1.5.2 Package Visibility Queries for UPI Apps** ✅
  - Added `<queries>` block with `intent` specifying `android.intent.action.VIEW` for `upi://pay`.

- **1.5.3 Security Hardening: Disable Data Backup** ✅
  - Set `android:allowBackup="false"` on `<application>` tag.

- **1.5.4 Application Class & Hilt Setup** ✅
  - Created `AurumApplication.kt` annotated with `@HiltAndroidApp`.
  - Registered `android:name=".AurumApplication"` in `AndroidManifest.xml`.
  - Annotated `MainActivity` with `@AndroidEntryPoint`.

- **1.5.5 Final Acceptance Verification** ✅
  - Verified project compiles, builds debug APK, and runs unit tests (`BUILD SUCCESSFUL in 1m 16s`, 50 actionable tasks).
  - All dependencies resolve without conflicts.

### ✅ Step 1 Acceptance Complete
- [x] Project compiles and runs Compose screen with AurumTheme.
- [x] All dependencies resolve without conflicts.
- [x] Debug APK packaged and unit tests passed.



---

## Step 2 — Domain Layer (Pure Kotlin, No Android) ✅

Build the business logic first so it can be unit-tested on the JVM without Robolectric.

### 2.1 Define domain models ✅
Created data classes and sealed interfaces in `domain/model/`:

| File | Contents | Status |
|---|---|---|
| `UpiPayload.kt` | `data class UpiPayload(vpa, payeeName, amount: BigDecimal?, note, txnRef, rawUri)` | ✅ |
| `UpiApp.kt` | `data class UpiApp(packageName, label)` (icon resolved in UI layer) | ✅ |
| `PaymentOutcome.kt` | `sealed interface PaymentOutcome { Success, Pending, Failed, Unconfirmed }` | ✅ |
| `Transaction.kt` | `data class Transaction(id, vpa, payeeName, amountPaise, note, …, status, createdAt, updatedAt)` | ✅ |
| `TransactionStatus.kt` | `enum: INITIATED, SUCCESS, PENDING, FAILED, UNCONFIRMED, USER_CONFIRMED_PAID` | ✅ |
| `ScanError.kt` | `sealed interface ScanError { NotUpiQr, InvalidVpa, UnsupportedCurrency, InvalidAmount }` | ✅ |

### 2.2 Implement `ParseUpiQr` use case ✅

- **2.2.1 Implement `ParseUpiQr.kt`** ✅
  - File: `domain/usecase/ParseUpiQr.kt`
  - Parses raw QR strings using `java.net.URI` without Android SDK dependencies.
  - Validation rules enforced:
    - Scheme must be `upi`, host must be `pay`.
    - `pa` (VPA) is required and validated with `^[a-zA-Z0-9._-]{2,256}@[a-zA-Z]{2,64}$`.
    - `cu` must be `INR` (case-insensitive) or absent.
    - `am` must be a positive number with <= 2 significant decimal places (relying solely on `stripTrailingZeros().scale() <= 2`), sanity-capped at ₹10,00,000. `am=0` or `am=0.00` is treated as a dynamic/user-entered open amount (`amount = null`).
    - Strips ASCII control characters and caps lengths for `pn`, `tn`, and `tr`.
  - Returns `Result<UpiPayload>` (success or typed `ScanError` failure).

- **2.2.2 Unit test `ParseUpiQr`** ✅
  - File: `test/java/com/example/aurum/domain/usecase/ParseUpiQrTest.kt`
  - 34 JVM unit tests covering: minimal QR, full merchant QR, case-insensitive scheme, absent currency, URL-encoded fields, unexpected query parameters, decimal validations with `stripTrailingZeros`, dynamic null amount for `am=0` and `am=0.00`, invalid schemes/hosts, malformed VPAs, non-INR currencies, negative/high amounts, control character sanitization.
  - Verified: 34 tests executed, 0 failures, 0 errors.

### 2.3 Implement `ValidateAmount` use case ✅

- **2.3.1 Implement `ValidateAmount.kt`** ✅
  - File: `domain/usecase/ValidateAmount.kt`
  - Accepts user input string and optional configurable `maxAmount` (default ₹1,00,000).
  - Validation rules enforced:
    - Non-blank.
    - Valid number.
    - Positive (> 0).
    - At most 2 decimal places (using `stripTrailingZeros()`).
    - Does not exceed `maxAmount`.
  - Returns `Result<BigDecimal>` or typed `AmountError` (`Empty`, `NotANumber`, `NotPositive`, `TooManyDecimals`, `ExceedsCap`).

- **2.3.2 Unit test `ValidateAmount`** ✅
  - File: `test/java/com/example/aurum/domain/usecase/ValidateAmountTest.kt`
  - 21 JVM unit tests covering: whole numbers, 1 & 2 decimals, trailing zeros, minimum ₹0.01, maximum ₹1,00,000, custom cap, whitespace stripping, blank strings, non-numeric strings, 0 / 0.00, negative values, 3+ decimals, amounts exceeding cap.
  - Verified: 21 tests executed, 0 failures, 0 errors.

### 2.4 Implement `BuildUpiUri` use case ✅

- **2.4.1 Implement `BuildUpiUri.kt`** ✅
  - File: `domain/usecase/BuildUpiUri.kt`
  - If QR already contains an amount (`payload.amount != null`), returns `payload.rawUri` unchanged to preserve merchant cryptographic signatures and parameters.
  - If no amount in QR, validates `userAmount > 0`, formats to 2 decimal places, and appends `am=<value>&cu=INR`.

- **2.4.2 Unit test `BuildUpiUri`** ✅
  - File: `test/java/com/example/aurum/domain/usecase/BuildUpiUriTest.kt`
  - 10 JVM unit tests covering: raw URI preservation when amount preset, user amount formatting, query parameter appending, invalid null/zero/negative amounts.
  - Verified: 10 tests executed, 0 failures, 0 errors.

### 2.5 Implement `InterpretPaymentResult` use case ✅

- **2.5.1 Implement `InterpretPaymentResult.kt`** ✅
  - File: `domain/usecase/InterpretPaymentResult.kt`
  - Accepts a map of key-value response extras from the UPI app result.
  - Case-insensitive status mapping: `SUCCESS` -> `PaymentOutcome.Success`, `FAILURE`/`FAILED` -> `PaymentOutcome.Failed`, `PENDING`/`SUBMITTED` -> `PaymentOutcome.Pending`, unknown/empty -> `PaymentOutcome.Unconfirmed`.
  - Extracts `txnId`, `approvalRefNo`, `responseCode`, and `message`.

- **2.5.2 Unit test `InterpretPaymentResult`** ✅
  - File: `test/java/com/example/aurum/domain/usecase/InterpretPaymentResultTest.kt`
  - 17 JVM unit tests covering: SUCCESS, FAILURE, FAILED, PENDING, SUBMITTED, UNCONFIRMED, case insensitivity, empty map, null values, blank fields.
  - Verified: 17 tests executed, 0 failures, 0 errors.

### 2.6 Define repository interfaces & query use case ✅

- **2.6.1 Create `TransactionRepository.kt`** ✅
  - File: `domain/repository/TransactionRepository.kt`
  - CRUD contracts: `insert(transaction)`, `updateStatus(...)`, `getAllTransactions(): Flow<List<Transaction>>`, `getInitiatedTransactions(): List<Transaction>`, `deleteAll()`.

- **2.6.2 Create `UpiAppRepository.kt`** ✅
  - File: `domain/repository/UpiAppRepository.kt`
  - Contract: `getInstalledApps(): List<UpiApp>`.

- **2.6.3 Create `PreferencesRepository.kt`** ✅
  - File: `domain/repository/PreferencesRepository.kt`
  - DataStore contracts: `getLastUsedUpiApp(): Flow<String?>`, `setLastUsedUpiApp(packageName)`.

- **2.6.4 Create `GetInstalledUpiApps.kt` use case** ✅
  - File: `domain/usecase/GetInstalledUpiApps.kt`
  - Injected domain use case delegating to `UpiAppRepository`.

### ✅ Step 2 Acceptance Complete
- [x] All 80 domain JVM unit tests passing without errors or failures (0 emulator/Robolectric dependencies).
- [x] Comprehensive test matrix covering valid/invalid QR, VPA formats, amounts, currencies, URI preservation, status interpretations, and boundary conditions.
- [x] All repository contracts cleanly isolated in pure Kotlin domain layer.

---

## Step 3 — Data Layer (Room, DataStore, PackageManager) ✅

Wire up persistence and platform access behind the repository interfaces.

### 3.1 Room database setup ✅

- **3.1.1 Implement `TransactionEntity.kt`** ✅
  - File: `data/db/TransactionEntity.kt`
  - Table name `transactions` with `@PrimaryKey id: String`.
  - Column mappings: `vpa`, `payee_name`, `amount_paise`, `note`, `qr_txn_ref`, `upi_txn_id`, `app_package`, `status`, `created_at`, `updated_at`.
  - Indices on `status` and `created_at`.
  - Extension mappers: `TransactionEntity.toDomain(): Transaction` and `Transaction.toEntity(): TransactionEntity` with safe enum fallback to `UNCONFIRMED`.

- **3.1.2 Implement `TransactionDao.kt`** ✅
  - File: `data/db/TransactionDao.kt`
  - `@Insert(onConflict = OnConflictStrategy.REPLACE)` `insertTransaction(entity)`
  - `@Query("UPDATE transactions SET status = :status, upi_txn_id = COALESCE(:upiTxnId, upi_txn_id), updated_at = :updatedAt WHERE id = :id")`
  - `@Query("SELECT * FROM transactions ORDER BY created_at DESC")` `fun getAllTransactions(): Flow<List<TransactionEntity>>`
  - `@Query("SELECT * FROM transactions WHERE status = 'INITIATED' ORDER BY created_at DESC")` `suspend fun getInitiatedTransactions()`
  - `@Query("DELETE FROM transactions")` `suspend fun deleteAll()`

- **3.1.3 Implement `AppDatabase.kt`** ✅
  - File: `data/db/AppDatabase.kt`
  - `@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)`
  - Abstract getter: `abstract fun transactionDao(): TransactionDao`.

### 3.2 Implement `TransactionRepositoryImpl` ✅
- File: `data/db/TransactionRepositoryImpl.kt`
- Implements `TransactionRepository` domain contract.
- Maps between `TransactionEntity` and domain `Transaction`.
- Exposes `getAllTransactions()` as `Flow<List<Transaction>>`.

### 3.3 Implement `PreferencesRepositoryImpl` ✅
- File: `data/prefs/PreferencesRepositoryImpl.kt`
- Uses Jetpack DataStore Preferences (`aurum_preferences`).
- Key: `KEY_LAST_USED_UPI_APP`.
- Emits updates via `Flow<String?>` and maintains in-memory `lastUsedAppCached` for non-blocking reads.

### 3.4 Implement `UpiAppRepositoryImpl` ✅
- File: `data/upi/UpiAppRepositoryImpl.kt`
- Uses `PackageManager.queryIntentActivities()` with `upi://pay` probe intent.
- Supports both Android 13+ (`PackageManager.ResolveInfoFlags`) and legacy APIs.
- Filters out self, removes duplicates by package name.
- Sorts results prioritizing the last-used app first, followed by alphabetical order.

### 3.5 Configure Hilt DI modules ✅
- `di/DatabaseModule.kt`: Provides `AppDatabase` singleton with destructive migration fallback and `TransactionDao`.
- `di/DataStoreModule.kt`: Provides `DataStore<Preferences>` singleton.
- `di/RepositoryModule.kt`: Binds `TransactionRepositoryImpl`, `PreferencesRepositoryImpl`, and `UpiAppRepositoryImpl` to their domain interfaces.

### ✅ Step 3 Acceptance Complete
- [x] Room database entity, DAO, and database compile cleanly with KSP.
- [x] Unit tests for entity mapping (`TransactionEntityTest`), repository operations (`TransactionRepositoryImplTest`), and preference storage (`PreferencesRepositoryImplTest`) pass with 100% success.
- [x] Debug APK assembled successfully (`:app:assembleDebug`) with all Hilt components, Room DAO implementations, and dex artifacts generated.

---

## Step 4 — QR Scanning (CameraX + ML Kit) ✅

### 4.1 Camera permission flow ✅
- **File**: `ui/scan/CameraPermissionHandler.kt`
- Implements `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`.
- Lifecycle-aware observer updates status automatically on resume.
- Three visual states:
  - `GRANTED`: executes camera content block.
  - `DENIED_RATIONALE`: shows clear explanation with "Grant Permission" CTA and gallery import fallback.
  - `PERMANENTLY_DENIED`: shows explanation with "Open Settings" CTA and gallery import fallback.

### 4.2 CameraX preview composable ✅
- **File**: `ui/scan/CameraPreview.kt`
- Encapsulates CameraX `PreviewView` using `AndroidView`.
- Binds `Preview` and `ImageAnalysis` to `LocalLifecycleOwner`.
- Back-pressure: `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`.
- Target resolution: `1280x720` for optimal scanning latency and low thermal impact.

### 4.3 QR code analyzer ✅
- **File**: `ui/scan/QrCodeAnalyzer.kt`
- Configured specifically for `Barcode.FORMAT_QR_CODE`.
- Safely converts `ImageProxy` to `InputImage` and guarantees `imageProxy.close()` is invoked on completion.
- Thread-safe `AtomicBoolean` lock prevents rapid duplicate scans or duplicate navigation triggers.
- Provides `resumeScanning()` to reset the lock upon screen re-entry or retry.

### 4.4 Scanner UX extras ✅
- **Torch toggle**: Reactive `isTorchEnabled` triggers `cameraControl.enableTorch(...)`.
- **Tap-to-focus**: `pointerInput` uses `previewView.meteringPointFactory` and `cameraControl.startFocusAndMetering(...)` with a 3-second auto-cancel.
- **Pinch-to-zoom**: `pointerInput` with `detectTransformGestures` calculates zoom delta and calls `cameraControl.setZoomRatio(...)` bounded by camera hardware limits.
- **Viewfinder overlay** (`ui/scan/ViewfinderOverlay.kt`):
  - 4 outer scrim rectangles for smooth hardware-accelerated cutout.
  - Aurum gold corner reticles framing the 260dp scan box.
  - Animated horizontal scanning laser beam with bidirectional tween easing.
  - Clear instructional guidance label positioned below the scan window.

### 4.5 Gallery QR import ✅
- **File**: `ui/scan/GalleryQrDecoder.kt`
- Coroutine-based singleton decoder running on `Dispatchers.IO`.
- Uses `InputImage.fromFilePath(context, uri)` and Google ML Kit scanner.
- Emits typed errors (`GalleryDecodeException.NoQrFound` / `ReadFailed`).

### ✅ Step 4 Acceptance Complete
- [x] CameraX preview, permission handling, analyzer, viewfinder, and gallery decoder implemented and verified.
- [x] Zero compilation warnings; `:app:assembleDebug` and `:app:testDebugUnitTest` succeed with 100% pass rate.

---

## Step 5 — UI Screens (Jetpack Compose + Navigation)

### 5.1 Navigation graph
- `ui/navigation/AppNavigation.kt`
- Define routes: `scan`, `confirm/{payload}`, `amount/{payload}`, `result/{txnId}`, `history`.
- Use `NavHost` with Compose Navigation.
- Consider a shared `PaymentFlowViewModel` scoped to the nav graph for cross-screen state.

### 5.2 ScanScreen
- `ui/scan/ScanScreen.kt` + `ScanViewModel.kt`
- Full-screen camera preview with viewfinder overlay.
- Top bar: torch toggle button, gallery import button.
- On QR detected → navigate to Confirm or Amount screen.
- Handle permission states inline.

### 5.3 ConfirmPayeeScreen
- `ui/confirm/ConfirmPayeeScreen.kt`
- Display: payee name (large), VPA (monospaced), amount (if present, read-only).
- Warn if payee name is empty or VPA handle looks unusual.
- "Continue" button → navigate to Amount (if no amount) or AppPicker (if amount present).

### 5.4 AmountScreen
- `ui/amount/AmountScreen.kt` + `AmountViewModel.kt`
- Custom numeric keypad (large touch targets).
- Live formatting with Indian locale (₹ symbol, lakh/crore separators).
- Inline validation error text below the amount field.
- "Pay ₹X" button → navigate to AppPicker.

### 5.5 UpiAppPickerSheet
- `ui/picker/UpiAppPickerSheet.kt`
- Modal bottom sheet listing installed UPI apps (icon + label).
- Last-used app pre-selected / shown first.
- Empty state: "No UPI app installed" with a Play Store link.
- On app tap → save preference, create pending transaction, launch payment.

### 5.6 ResultScreen
- `ui/result/ResultScreen.kt`
- Four visual states:

| State | Visual |
|---|---|
| **Success** | Green checkmark animation, "Reported by your UPI app" disclaimer |
| **Pending** | Amber clock icon, "Check your UPI app for status" |
| **Failed** | Red X, response code detail, "Retry" button |
| **Unconfirmed** | Gray question mark, "Did the payment go through?" toggle for manual confirmation |

- "Scan another" button → navigate back to `scan`.
- "View history" link.

### 5.7 HistoryScreen
- `ui/history/HistoryScreen.kt` + `HistoryViewModel.kt`
- Lazy list of past transactions (payee, amount, status, date).
- Status chip with color coding.
- "Clear history" action in the top bar.

### 5.8 Shared UI state
- `ui/scan/PayUiState.kt` — sealed interface as described in the architecture report.
- The `PaymentFlowViewModel` (or per-screen VMs) exposes `StateFlow<PayUiState>`.

### ✅ Acceptance
- All screens render correctly on multiple screen sizes.
- Navigation transitions are smooth.
- Amount formatting displays ₹1,00,000 correctly.
- App picker shows real installed UPI apps.
- Result screen shows all 4 outcome states.

---

## Step 6 — Payment Launch & Result Handling

### 6.1 Activity Result contract
- Register an `ActivityResultLauncher<Intent>` using `StartActivityForResult` contract.
- This should be set up in the hosting `Activity` or `ComposableActivity` and shared via the ViewModel / a callback.

### 6.2 Build and launch the payment intent
- In the ViewModel (or a dedicated `PaymentLauncher` class):
  1. Build the URI using `BuildUpiUri` use case.
  2. Create `Intent(ACTION_VIEW, uri).setPackage(selectedApp.packageName)`.
  3. **Before launching**: save a `Transaction` with status `INITIATED` to Room (generates UUID).
  4. Launch via the `ActivityResultLauncher`.
  5. Catch `ActivityNotFoundException` → show error, return to picker.

### 6.3 Handle the result callback
- In the `onActivityResult` callback:
  1. Call `InterpretPaymentResult` use case with the returned `Intent`.
  2. Update the Room transaction with the resolved status and `txnId`.
  3. Navigate to `ResultScreen`.

### 6.4 Process-death reconciliation
- On app cold start (in `Application.onCreate` or a `ViewModel.init`):
  1. Query Room for transactions with status `INITIATED`.
  2. If found, show a reconciliation prompt: "A payment was in progress. Did it complete?"
  3. Let the user mark it as paid or failed.

### 6.5 Double-tap prevention
- Debounce the "Pay" / app-select button (disable it for 1–2 seconds after first tap).
- Prevent re-navigation while `AwaitingResult` state is active.

### ✅ Acceptance
- Payment intent opens the correct UPI app.
- Returning from the UPI app correctly shows Success / Pending / Failed / Unconfirmed.
- Killing the app during payment → reconciliation prompt on next open.
- Double tapping the pay button does not launch two intents.

---

## Step 7 — Testing

### 7.1 Unit tests (JVM)
- **Parser tests** (`ParseUpiQrTest.kt`):
  - Valid standard QR, valid merchant QR with all fields.
  - Missing `pa` → failure.
  - Invalid VPA format → failure.
  - Invalid amount (negative, too many decimals, non-numeric) → failure.
  - Non-INR currency → failure.
  - URL-encoded characters in `pn` and `tn`.
  - Extra/unknown parameters → still succeeds.
  - Empty string, random text, HTTP URLs → failure.
- **Amount validation tests** (`ValidateAmountTest.kt`):
  - Valid amounts, zero, negative, > cap, > 2 decimals, non-numeric.
- **URI builder tests** (`BuildUpiUriTest.kt`):
  - QR with amount → raw URI unchanged.
  - QR without amount → `am` and `cu` appended.
- **Result interpreter tests** (`InterpretResultTest.kt`):
  - SUCCESS, FAILURE, SUBMITTED, PENDING, empty intent, null data, mixed-case keys.

### 7.2 ViewModel tests
- Use **Turbine** to collect `StateFlow` emissions.
- Test state transitions: `Scanning` → `Confirm` → `EnterAmount` → `ChooseApp` → `AwaitingResult` → `Result`.
- Use fake repositories injected via Hilt testing.

### 7.3 UI / Compose tests
- **ScanScreen**: permission prompt renders, camera preview placeholder.
- **ConfirmScreen**: payee name and VPA displayed correctly.
- **AmountScreen**: keypad input updates display, validation error shown.
- **AppPickerSheet**: list of apps renders, tap fires callback.
- **ResultScreen**: correct icon/color for each outcome.

### 7.4 Integration / Intent tests
- **Espresso-Intents**: stub the UPI intent to return canned results (SUCCESS, FAILURE, SUBMITTED, empty, cancelled) and verify the result screen displays correctly.

### 7.5 Manual device testing
- Test on real devices with: PhonePe, Google Pay, Paytm, BHIM.
- Test on Android 11 (package visibility), 13, and latest.
- Use small amounts to a second personal account.
- Test with printed QR codes, on-screen QR codes, low light, glare, tilted angles.

### ✅ Acceptance
- ≥ 90% line coverage on the domain layer.
- All ViewModel state transitions covered.
- Espresso intent stubs cover all 4 outcome states.
- No crashes on tested devices.

---

## Step 8 — Security Hardening, Polish & Release

### 8.1 Security hardening
- [ ] `android:allowBackup="false"` in manifest.
- [ ] No payment details in `Log.*` calls (strip in release builds via R8/ProGuard).
- [ ] `FLAG_SECURE` on payment confirmation and result screens (optional).
- [ ] Enable R8 full mode with `minifyEnabled true` and `shrinkResources true`.
- [ ] Review all exported components — nothing should be exported that doesn't need to be.

### 8.2 Accessibility
- Content descriptions on all icons and images.
- Touch targets ≥ 48dp.
- Screen reader testing with TalkBack.
- Sufficient color contrast ratios (WCAG AA).

### 8.3 Localization
- Extract all strings to `strings.xml`.
- Add Hindi (`values-hi/`) and other regional language translations.
- RTL layout testing.

### 8.4 Process-death & config-change resilience
- Ensure `SavedStateHandle` or Room-backed state survives:
  - Screen rotation.
  - Background process kill.
  - Split-screen transitions.

### 8.5 Play Store preparation
- Write the Play Store listing (title, description, screenshots).
- Create a privacy policy (hosted URL) — must cover what data is stored locally.
- Complete the **Financial Features declaration** and **Data Safety** form.
- Prepare a signed release APK/AAB.

### 8.6 Beta release
- Publish to a closed testing track.
- Gather feedback from beta testers.
- Monitor crash reports (Firebase Crashlytics or Play Console).

### ✅ Acceptance
- No sensitive data in logs.
- App passes TalkBack navigation.
- Hindi strings render correctly.
- App survives process death mid-payment.
- Play Store listing is complete and privacy policy is published.

---

## Dependency Graph (Build Order)

```mermaid
graph TD
    S1["Step 1: Project Setup"] --> S2["Step 2: Domain Layer"]
    S1 --> S3["Step 3: Data Layer"]
    S2 --> S4["Step 4: QR Scanning"]
    S2 --> S5["Step 5: UI Screens"]
    S3 --> S5
    S4 --> S5
    S5 --> S6["Step 6: Payment Flow"]
    S2 --> S7["Step 7: Testing"]
    S3 --> S7
    S5 --> S7
    S6 --> S7
    S7 --> S8["Step 8: Hardening & Release"]
```

> [!IMPORTANT]
> **Steps 2 and 3** can be worked on in parallel after Step 1 is complete. **Step 4** (scanning) and **Step 5** (UI) depend on the domain models from Step 2. **Step 6** (payment flow) wires everything together. **Step 7** (testing) should be done continuously but has a dedicated pass after Step 6. **Step 8** is the final polish before release.

---

## Estimated Timeline

| Step | Effort | Cumulative |
|---|---|---|
| 1 — Project Setup | 1 day | Day 1 |
| 2 — Domain Layer | 2–3 days | Day 3–4 |
| 3 — Data Layer | 2 days | Day 3–4 (parallel with 2) |
| 4 — QR Scanning | 2–3 days | Day 6–7 |
| 5 — UI Screens | 3–4 days | Day 10–11 |
| 6 — Payment Flow | 2–3 days | Day 13–14 |
| 7 — Testing | 3–4 days | Day 17–18 |
| 8 — Hardening & Release | 3–5 days | Day 21–23 |
| **Total** | **~3–4 weeks** | |
