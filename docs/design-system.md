# Design system and resource inventory

## Supplied resources

Initially, the local Git checkout and remote repository contained no implementation, assets or design files. GitHub access to `tinyarttales42-bot/byos-android` was confirmed with push/admin permission. It was public, contrary to the initial private-repository brief; the owner explicitly authorized the existing public repository.

The owner then supplied one 3000×2000 JPEG theme reference (clipboard attachment ending `33ba.jpg`). It shows three rounded phone panels, translucent surfaces, pink→violet gradients, blue glows, music-note tiles, circular vinyl artwork, and a bottom navigation bar. Placeholder text is not product copy. No Figma source, licensed album art, font package or additional BYOS screens were supplied. Their contents have not been invented.

The reference provides visual direction. Its original image is not redistributed in this public repository because its provenance/license was not supplied. App icons, gradients and vinyl drawing are implemented in Compose/vector resources. No generated or stock artwork is required.

## Adaptation

The original brief calls for a premium dark UI. BYOS adapts the reference's gradients and rounded translucent panels onto a near-black purple canvas; body text uses high-contrast neutrals.

| Token | Value / use |
|---|---|
| Ink | `#10101B` background |
| Panel | `#1C1B2D` elevated surface |
| Lavender | `#CEACFF` primary controls |
| Pink | `#F3A5DB` ratings and gradient highlight |
| Blue | `#8EBDFF` offline status and cool accent |
| Muted | `#B5B1C8` secondary copy |
| Spacing | 4/8/12/16/20/24/32 dp |
| Shapes | 14 dp art tiles, 18 dp fields, 20 dp mini player, 24 dp cards |
| Type | System sans; 32/26 sp headings, 22/16 sp titles, 14 sp body |

## Navigation and components

Bottom navigation: Library / Playlists / Tags. Playlist and tag details filter the library. A mini player opens full Now Playing. System Back closes player/detail before returning to Library. Shared components: `GlassCard`, `RecordArt`, `SongRow`, `RatingBar`, naming dialog and empty states.

Material controls use 48 dp targets; meaningful icons have accessibility labels. Decorative icons do not announce redundant names. Song titles truncate in rows; player content scrolls on small screens/large fonts. Playback is disabled for samples. Device screenshots and actual verification coverage are in the verification report. TalkBack, 200% fonts and tablet/landscape review are release gates, not assumed passes.
