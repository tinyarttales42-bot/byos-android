# Sprint backlog

## Sprint 0 — foundation

- [x] Confirm GitHub access; document public-repository authorization.
- [x] Inventory empty repository and supplied theme reference.
- [x] Establish sprint branch, Gradle wrapper, pinned dependencies and Android module.
- [x] Theme tokens, components, navigation and fictional sample library.
- [x] MediaStore scan, Media3 service/controller and Room metadata foundation.
- [x] Unit, Room instrumentation and Compose navigation tests.
- [x] CI build/lint/tests and APK/report uploads.
- [x] Architecture, design inventory, backlog and release checklist.
- [ ] Complete verification and attach results/screenshots to Sprint 0 PR (actual status in verification report).

## Sprint 1 — priority order

1. **Permission/indexing reliability:** API 26/32/33/35/current-stable matrix, denial/revocation, multi-volume audio, content observer, cancellation, missing/deleted files and persistent scan cache. Acceptance: no crash or stale playable entry after revoke/delete; large scans off main thread.
2. **Playback lifecycle:** process-death/queue restoration, notification/lock screen, audio focus/ducking, Bluetooth/headsets, background restrictions and corrupt files. Acceptance: device-tested continuation, pause/resume and readable errors.
3. **Metadata:** tag removal/rename/delete, playlist ordering, blank/duplicate/concurrent operations, persistence and migrations. Acceptance: invariants survive restart and concurrent updates.
4. **UI:** embedded artwork, filter reset UX, tablet/landscape, TalkBack, 200% fonts, localization and screenshot regression. Validate owner's theme feedback.
5. **Toolchain/security:** update dependencies/target SDK after compatibility review; pin CI actions by verified commit; dependency monitoring and required branch checks; confirm application ID.
6. **Release:** signing-key custody, backup/export policy, current Play declarations/privacy materials, performance/battery measurements, release/R8 testing and approved internal distribution.

## Definition of done

Acceptance tests and required CI pass on PR head; no critical playback/data-loss bug; screenshots represent the built app; blockers explicit; owner reviews. Production release requires separate approval and release checklist completion.
