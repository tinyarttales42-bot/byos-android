# Architecture

## Boundaries

```text
Compose UI → LibraryViewModel → LibraryRepository → MediaStore / Room
          → PlaybackConnection → MediaController → PlaybackService
                                                   └─ MediaSession + ExoPlayer
```

The single `app` module uses package boundaries (`model`, `data`, `playback`, `ui`). Split modules when ownership/build size justifies it. The application owns a lazy database/repository; the ViewModel owns screen state and its controller connection. Compose collects lifecycle-aware flows. UI never queries Room or MediaStore directly.

## Source of truth

- GitHub: source code, reviewed commits and versioned Room schemas.
- MediaStore: device audio index. `content://` URIs identify audio; raw filesystem paths are not stored.
- Room: user metadata, playlist membership and tag relationships. Song lists refresh explicitly and on app startup; a persistent scan cache and content observer are Sprint 1 work.
- Sample data: fictional, in memory, labeled, with no playable URIs. Samples cannot enter persistent playlists or be tagged. Sample ratings are temporary.

## Persistence and invariants

Schema v1 contains `playlists`, `playlist_songs`, `tags`, `song_tags`, and `ratings`. Playlist/tag names are trimmed, internal whitespace collapsed and keys lowercased with `Locale.ROOT`. Unique indexes prevent duplicate names, including concurrent writes. Junction composite keys prevent duplicate assignments/membership. Deleting a playlist cascades membership but never deletes audio, ratings or tags. Repository validates 1–5 ratings. Zero in the UI means unrated, not a saved rating.

No destructive migration fallback is enabled. Schema changes must add explicit migrations and migration tests. Room schemas are checked into `app/schemas`. Missing audio retains metadata for now; reconciliation after deletion/reindexing/SD-card changes is Sprint 1 work.

## Permissions and playback

API 33+ uses `READ_MEDIA_AUDIO`; API 26–32 uses `READ_EXTERNAL_STORAGE` with maxSdkVersion 32. Denied permission offers Settings and samples. Returning from Settings rechecks access. Scanning runs on IO and reports provider/security failures. App launch does not play automatically.

Media3 owns playback in a `MediaSessionService`, separate from activity lifetime. Its media notification is managed by Media3; the service declares foreground-service permissions and `mediaPlayback` type. Player handles audio focus and becoming-noisy events. Session activity returns to BYOS. Controller listeners synchronize actual playback status; UI does not simulate audio. Player/controller/session resources are released by their owners.

No `INTERNET` permission or audio uploads. Media3 contributes `ACCESS_NETWORK_STATE` in the merged manifest; it does not permit network connections. Automatic backup is disabled until a user-controlled metadata backup policy is designed.

## Toolchain decision

Sprint 0 pins the compatible installed toolchain: AGP 8.7.2, Gradle 8.9, Kotlin/Compose compiler 2.0.21, Compose BOM 2024.11.00, Room 2.6.1, Media3 1.5.1, compile/target SDK 35, min SDK 26, Java 17. This is a reproducible baseline, **not a claim these are the latest versions**. Upgrade and revalidate current Android/Play requirements before production submission.

References: [AGP compatibility](https://developer.android.com/build/releases/agp-8-7-0-release-notes), [Media3 background playback](https://developer.android.com/media/media3/session/background-playback).

## Known limits

- Queue/process-death restoration, gapless verification, large-library performance, artwork extraction and SD-card reconciliation remain open.
- English strings are a prototype baseline; localization extraction is scheduled before release.
- Navigation is a small saveable tab/detail state machine with Back handling. Add Navigation Compose/deep links when destinations expand.
- No Play Console, production signing identity or application-ID ownership has been established.
