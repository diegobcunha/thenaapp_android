# SDD — Create Baby Feature

## Decisions Made

- **Multi-step pager**: `HorizontalPager` with `userScrollEnabled = false`. Navigation is button-driven only. System back press (or TopAppBar back arrow) on page > 0 goes to the previous page; on page 0 it pops the nav stack.
- **BabyGender**: `enum class BabyGender { GIRL, BOY, OTHER }` in `feature/baby/domain/model/`. The datasource layer serializes it as `gender.name.lowercase()`.
- **Date mask**: Locale-aware `VisualTransformation` (YYYY-MM-DD for non-PT locales, DD/MM/YYYY for PT). Raw state stores digits only (max 8 chars); the API always receives ISO 8601 (YYYY-MM-DD).
- **Photo**: Camera (FileProvider + `TakePicture`) and gallery (`PickVisualMedia`). The selected image is scaled to max 800px and JPEG-compressed at 80% quality before base64 encoding. Base64 string is sent in the API request as `photoBase64`.
- **User session**: New `UserSessionRepository` (interface in `:datasource`, backed by `CustomSharedPreferences`) persists the backend user UUID after login and signup. `CreateBabyRepositoryImpl` reads it to fill the `responsible` field.
- **API call timing**: Single `POST /v1/baby` on the "Start tracking 🌸" button (page 2). Pages 0 and 1 only do local validation.
- **Validation**: All fields required. Name + gender validated before advancing to page 1. Date (format + calendar validity) + weight + height validated before advancing to page 2.
- **Clean Architecture DTO rule**: `responsible` is filled by the repository layer (from session), keeping the domain `CreateBabyRequest` free of infrastructure concerns.

## Technical Features Implemented

| Layer | What was built |
|---|---|
| `datasource` | `UserSessionRepository` + `UserSessionRepositoryImpl`; `CustomSharedPreferences` extended with `putString`/`getString`; `CreateBabyRequest` model updated with `photoBase64` |
| `feature/login` | `LoginRepositoryImpl` saves user UUID to session on both email and Google login paths |
| `feature/signup` | `SignupRepositoryImpl` saves user UUID to session on both signup paths |
| `feature/baby` domain | `BabyGender` enum; domain `CreateBabyRequest` updated (removed `responsible`, typed gender, added `photoBase64`) |
| `feature/baby` MVI | `CreateBabyState` (currentPage, BabyGender, photoBase64, generalError); `CreateBabyIntent` (OnNextPage, OnPreviousPage, typed gender, photo base64); `CreateBabyViewModel` fully implemented |
| `feature/baby` repository | `CreateBabyRepositoryImpl` implemented via `safeApiCall` + session UUID |
| `feature/baby` UI | `DateMaskVisualTransformation`, `StepIndicator`, `GenderSelector`, `BabyInfoStep` (with full photo picker), `BirthDetailsStep`, `AllSetStep`, `CreateBabyScreen` |
| `app` | `AndroidManifest.xml` with CAMERA + storage permissions + `FileProvider`; `file_paths.xml`; `MainActivity` wired with `onNavigateBack` |

## Current Status

✅ Feature complete and compiling.

**Not yet wired**: `onNavigateToHome` in `MainActivity` is still an empty lambda — needs to be connected to the home destination once it exists.

**Known limitation**: `PickVisualMedia` on API < 21 falls back to `GetContent`, which on API 24–32 may require `READ_EXTERNAL_STORAGE` permission at runtime. The permission is declared in the manifest; runtime request is not implemented (the photo picker system UI handles it on API 33+).

## Last Updated

2025-04-23
