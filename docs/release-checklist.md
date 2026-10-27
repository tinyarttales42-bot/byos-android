# Release and rollback checklist

## Before merging

- [ ] PR has current head SHA, screenshots, test evidence and blockers.
- [ ] Build, lint, unit and emulator checks pass on that SHA.
- [ ] Review schema changes/migrations; no destructive fallback.
- [ ] No signing keys, credentials, private media or local config tracked.
- [ ] Review accessibility, empty/error/permission states and offline behavior.
- [ ] Require review/status checks on `main`; verify configured protections before merge.
- [ ] Preserve pre-merge base SHA and PR history.

## Before production

- [ ] Explicit owner approval for exact version and channel.
- [ ] Confirm application ID, current target API/Play requirements and dependency security.
- [ ] Validate release/R8 build on supported devices and run regression matrix.
- [ ] Set monotonic versionCode and semantic versionName. Tag approved merge commit `vMAJOR.MINOR.PATCH`.
- [ ] Signing keys stay in approved external custody. Never commit keys/passwords or expose secrets in logs/artifacts.
- [ ] Complete Play Console, signing, store assets, privacy/data safety, foreground-service declarations and test tracks.
- [ ] Record artifact checksum, certificate fingerprint, changelog and durable retention.
- [ ] Agree rollout, monitoring and rollback owner.

## Rollback

Pre-merge base: `65f3e2b8be28f5cc6874ddbc886b7572fe950227` (minimal repository initialization). Close an unmerged PR to abandon the sprint. After merge, revert through a new PR, preserving history; never force-push `main`. Schema changes need compatible forward migrations. Android does not normally permit versionCode downgrades: ship a corrected higher-code build. A Git tag is not a database rollback.

No production signing, Play Console, release tag or production publication is part of Sprint 0.
