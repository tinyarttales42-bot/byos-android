# Google Play release preparation

Status: **not published; owner account registration required**. Updated 27 October 2026.

## Confirmed by the owner

- Permanent application ID: `app.byos.android`.
- Intended developer name: `tinyarttales42`.
- Public support email: `tinyarttales42@gmail.com`.
- The owner has no Google Play developer account yet.
- Existing public GitHub repository is authorized for branches and PRs.

## Prepared in this branch

- API 36 target and compile SDK; AGP 8.13.2 and Gradle 8.13 with distribution checksum.
- Release minification, lint, unsigned AAB build and CI artifact.
- Optional upload signing supplied only through environment variables.
- Offline privacy policy accessible from the library toolbar, plus a public-page source in `docs/privacy/index.html`.
- Draft English store copy in `store/en-US/` and existing app screenshots in `docs/screenshots/`.

## Owner steps and external blockers

1. Create a [Google Play Console developer account](https://play.google.com/console/signup), accept Google's agreement, pay Google's US$25 one-time registration fee, and complete identity/contact verification. Select the account type matching the actual publisher. Do not send passwords, payment details or identity documents through chat.
2. For a new personal account, complete real Android device verification and the required closed test: at least 12 opted-in testers for 14 consecutive days, then apply for production access. Registration is not immediate production eligibility.
3. Provide an authenticated Play Console session after verification. Account-specific app creation, declarations and upload cannot be performed before this exists.
4. Establish owner-controlled upload-key custody and backup, and enroll in Play App Signing. No production key has been generated or supplied. The CI AAB is unsigned and **not upload-ready**.
5. Publish the privacy page at a stable public HTTPS URL and verify it before entering it in Console. The checked-in HTML is a source file, not a live policy URL.
6. Complete store assets (512×512 icon, 1024×500 feature graphic and current phone screenshots), content rating, target audience, app access, ads, data safety and foreground-service declarations. Confirm declarations against the final signed build; do not treat the draft below as submitted answers.
7. Complete release-device regression, accessibility/large-text review, permission revocation, interrupted playback, Bluetooth/headset and native 16 KB page-size validation. Review dependencies and release notes before signing the exact upload build.
8. Merge through reviewed, passing PRs. Preserve the Sprint 0 rollback point `a730802faff03c612eb1892fbaca5cc5f30edf8f`. Tag the approved release commit using semantic versioning and record the signed bundle checksum/certificate. No release tag has been created.

## Build and signing

```sh
./gradlew bundleRelease assembleRelease testDebugUnitTest lintRelease
```

Unsigned review bundle: `app/build/outputs/bundle/release/app-release.aab`.
Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`.

For the final upload, set `BYOS_UPLOAD_STORE_FILE`, `BYOS_UPLOAD_STORE_PASSWORD`, `BYOS_UPLOAD_KEY_ALIAS` and `BYOS_UPLOAD_KEY_PASSWORD` through a secure local environment or secret manager, then rebuild. Never place values in shell history, repository files, PRs or logs. Missing secrets fail configuration when a keystore path is supplied. Do not use a debug key for Play uploads.

## Declaration worksheet (draft, not submitted)

- Core purpose: user-initiated playback and organization of locally stored audio.
- Audio access: READ_MEDIA_AUDIO on API 33+; READ_EXTERNAL_STORAGE limited to API 32 and earlier. No all-files access.
- Foreground service: `mediaPlayback`; the user starts a song, playback continues with media notification/session controls and can be paused. Record a demonstration from the release build for Console if requested.
- BYOS has no login, ads, purchases, analytics or network permission. Music, metadata, playlists, tags and ratings are processed/stored locally. OS media controllers receive playback metadata. Data safety should reflect the final dependency and permission audit; support email is outside the app's data flow.
- No user account creation or server-side music storage. Local data can be removed through Android app storage settings/uninstall. Original audio remains untouched.
- Target audience and content rating require the owner's accurate audience/business choices and Console questionnaire; no answers have been invented.

## Official requirements checked

- [Registration, fee and verification](https://support.google.com/googleplay/android-developer/answer/6112435)
- [New personal account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- [AGP 8.13 compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Privacy policy and user data](https://support.google.com/googleplay/android-developer/answer/10144311)
- [Store preview asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151)
- [16 KB page sizes](https://developer.android.com/guide/practices/page-sizes)
