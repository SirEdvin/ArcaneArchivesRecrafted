# 0080 — Registered Echo item state

Status: item state/registration/name/tooltip implementation and original asset packaging verified; source-derived tint and runtime acceptance remain open.

Original: baseline `80944ce45c6559243d8928cc4b305bf379388652`, `items/EchoItem.java:17–65`, registered in `init/ItemRegistry.java:115,129`. `echoFromItem` copies one source item without consuming it. `itemFromEcho` returns the saved source without consuming the Echo. Empty Echoes retain the ordinary name and red invalid tooltip; populated Echoes have a typed name and gold contained-item tooltip. The animated three-layer model is original upstream artwork. The release pin does not contain EchoItem.

Scope/approval: existing approval to preserve registered reachable behavior, not to activate dormant machines. Implement registration, creative visibility, source state, names/tooltips and original model/assets on all four targets. No recipe, right-click conversion or furnace/matrix activation is introduced. The only upstream producer found is the unregistered Radiant Furnace tile; it remains disabled. Helpers do not themselves create a player-accessible duplication action.

Modern persistence uses the original `source` NBT on 1.20.1 and an isolated `arcanearchives:echo_source` data component containing native immutable ItemContainerContents on 1.21.1. This is not vanilla container storage: no new container interaction is exposed. Native codecs preserve source components and synchronize them without needing client globals to decode display names. Reads return copies and do not write empty tags, retaining existing conservation/read-only safety corrections. Old-world conversion remains out of scope.

Affected targets: 1.20.1 Fabric/Forge, 1.21.1 Fabric/NeoForge.

Reachability follow-up: [0109](0109-radiant-furnace-runtime-proposal.md) traces direct construction of furnace tiles by the registered block and initialization of its ore predicate. Their missing tile registration alone does not prove the machine's code is dormant. The user subsequently [excluded the entire furnace feature](0110-radiant-furnace-exclusion.md); that proposal is closed. Independent Echo item/state/tint support is retained without a furnace producer.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*EchoItemTest' --offline --no-daemon`, exit 0, 28 seconds, `build/echo-item-20260909-175519.log`. All four targets assembled; loader-aware NeoForge XML reports 3 tests, zero failures/errors/skips. Tests cover source count/components and defensive copies, no consumption, native saved-item and network codec round trips, empty source, typed names and read-only tooltip lookup. These are fixtures, not actual world-restart or multiplayer tests.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, reported duration 0 seconds, `build/echo-item-artifacts.log`; all four production/source pairs pass, including Echo classes/source and all six model/texture/animation assets. Those six copied files are byte-identical to the upstream baseline. `git diff --check` passed. Native mapped 1.21.1 ItemContainerContents and DataComponentType signatures were inspected before implementation. Upstream has no Portuguese Echo entries; the original English fallback remains rather than inventing an upstream translation.

Source-derived tint selection (`proxy/ClientProxy.java:100–118`, including TintUtils fallback), connected rendering/animation, multiplayer and world-restart acceptance remain separate open work. This increment must not be reported as complete Echo presentation or complete progression migration.
