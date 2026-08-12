<div align="center">

<sub><a href="CHANGELOG.md">Polski</a> · <b>English</b></sub>

</div>

# Changelog

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
versioning follows [SemVer](https://semver.org/).

## [1.11.3] — 2026-08-12

First public release. Until now the mod only ran on the a private server servers.

### Fixed

- **The gesture wheel and item sign screen titles** were hardcoded in Polish. They are only
  ever read out by the narrator, which means a player using a screen reader got Polish text
  no matter which language they had set. Both now go through `en_us`/`pl_pl`.

### Changed

- English mod metadata, author and repository links in `fabric.mod.json`.
- Added `LICENSE` (MIT), an English `README.en.md` and this changelog.

## [1.11.2] — 2026-08-11

### Changed

- A completed challenge is now visible in the class description (`/bmd`) and in `/bmd mode` —
  previously nothing said so once the restrictions were lifted.

## [1.11.1] — 2026-08-11

### Added

- Challenge status shown when a player joins the server.

### Changed

- `/bmd mode` is saved to the config, so it survives a server restart.

## [1.11.0] — 2026-08-10

### Added

- **3D echolocation.** A sound fires a wave that lights up the shape of the surroundings
  instead of drawing a flat outline. Density raised to 480 rays from 96.

### Fixed

- The blind player's HUD was mirrored: the camera's `right` vector carried the left-hand sign.

## [1.10.0] — 2026-08-06

### Changed

- Completing a goal lifts every restriction at once — they used to be removed by hand.

## [1.9.0] — 2026-08-06

### Added

- PL/EN translations of every message.
- 60 challenge goals across three difficulties, plus a timer.

## [1.8.0] — 2026-08-06

### Added

- Echolocation in three dimensions (it used to be flat) and class emoji.
- 1.8.1: icons for blocks as well, not just items.

## [1.7.0] — 2026-08-06

### Changed

- Gestures are drawn on the player's nametag instead of custom bubbles — visible from any
  distance and no longer covering the world.

## [1.6.0] — 2026-08-06

### Changed

- The deaf player can eat despite the right-click block.
- Only the blind player may craft.

## [1.0.0] – [1.5.0] — 2026-08-06

Internal versions, one day of fast iteration: the three classes and the plugging of holes in
blindness and deafness, blindness modes (`EASY`/`NORMAL`/`HARD`), the blind player's HUD,
12 gestures, the LCtrl/LAlt keys, the login block for clients without the mod, and the mod's
own font so gesture emoji render as emoji rather than squares.
