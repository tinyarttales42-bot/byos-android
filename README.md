# BYOS — Bring Your Own Song

An offline-first Android music player built with Kotlin, Jetpack Compose, Media3 and Room.

**Sprint 0 / 0.1.0 foundation.** This is a development build, not a production release. Core flows are implemented; device compatibility and release hardening remain in the backlog.

## Getting started

Requirements: JDK 17, Android SDK Platform 36, Build Tools 35.0.0, and an Android device/emulator running API 26+. Android Studio is optional. The checked-in Gradle 8.13 wrapper downloads dependencies on the first build; the installed app needs no internet access.

1. Clone this repository and check out the sprint branch.
2. Set `ANDROID_HOME` to the SDK directory, or create an untracked `local.properties` containing `sdk.dir=/path/to/Android/Sdk` (Windows supports forward slashes).
3. Run:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest # with an emulator/device connected
```

Windows: use `gradlew.bat`. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Debug package: `app.byos.android.debug`.

Install using `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Choose **Scan my music** and grant audio access. Only files already indexed by Android's MediaStore appear. **Explore sample library** shows fictional metadata with disabled playback; sample songs are never inserted into the device database.

## Included

- Dark violet design system inspired by the supplied pink/violet/blue glass and vinyl reference.
- Library, playlists, tags, mini player, and Now Playing; accessible control labels and scrollable layouts.
- Permission-aware MediaStore scanning (audio permission on API 33+, storage permission through API 32).
- Background Media3 playback with media notification controls, audio focus, unplug handling, seek, shuffle and repeat.
- Room-backed playlist create/rename/delete, song membership, normalized unique tags, tag assignment, and 1–5 star ratings.
- Search across title/artist/album/tags, title/artist/rating sort, favorites (4+ stars), and tag filtering.
- Gradle wrapper, version catalog, Room schema export, unit/device/UI tests and GitHub Actions APK/report artifacts.
- No `INTERNET` permission, analytics, accounts, signing secrets or bundled commercial music.

## Project guides

- [Architecture](docs/architecture.md)
- [Design system and reference inventory](docs/design-system.md)
- [Sprint backlog](docs/sprint-backlog.md)
- [Verification report](docs/verification.md)
- [Release and rollback checklist](docs/release-checklist.md)
- [Play release preparation and blockers](docs/play-release.md)

CI builds PRs and pushes to main, then runs emulator tests on APIs 26 and 36. Download `byos-debug-<commit>` from the Actions run for the APK. CI also checks the minified release build and provides `byos-release-UNSIGNED-<commit>` for review; this bundle cannot be uploaded to Play until signed. Debug signing is generated locally/by the runner and is never committed. CI artifacts expire after 14 days.

All feature work uses sprint branches and PRs. Do not merge failing checks. Production publication and signing require explicit owner approval. Release tags use semantic versions (`v0.1.0` for the first approved foundation release); no release tag is created merely by opening a sprint PR.
