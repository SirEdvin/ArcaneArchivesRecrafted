# Mixin minimum-version metadata and startup regression

## Change and scope

All three production Mixin configurations now declare `minVersion: "0.8.5"`. The previous chest acceptance exposed Forge's `Mixin config arcanearchives.forge.mixins.json does not specify "minVersion" property` diagnostic. The sibling common and Fabric-specific configurations also lacked this declaration and are fixed together.

The minimum matches the oldest pinned runtime actually observed: Forge 1.20.1 loads Mixin 0.8.5; both Fabric leaves and NeoForge load Mixin 0.8.7. This declares a supported baseline, not compatibility with arbitrary older loader versions. No mixin classes, injection targets, injection counts, refmap names, Java compatibility levels or gameplay behavior were changed. No third-party implementation was copied and no gameplay-deviation approval is required.

`scripts/verify_artifacts.py` now requires the explicit value on each packaged Arcane Archives Mixin JSON. Both native startup scripts reject the original missing-minimum diagnostic instead of treating normal exit as sufficient. These are additional packaging/startup validation checks, not suppressions of loader logging.

## Red/green evidence

- Before editing production resources, the strengthened artifact check failed against existing JARs: `1.20.1-fabric: missing or changed Mixin minimum version in arcanearchives.fabric.mixins.json`. Command `timeout --foreground 60s python3 scripts/verify_artifacts.py`; exit 1, under 1s, `build/mixin-metadata-red-20260910-123225.log`.
- Canonical build: `timeout --foreground 10m ./gradlew build --no-daemon`; exit 0, 49s, `build/mixin-metadata-build-20260910-123331.log`. All four artifact pairs then passed.
- Real startup matrix: `timeout --foreground 10m python3 scripts/smoke_servers.py`, followed by `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>` for each leaf. Exit 0, aggregate 174s, `build/mixin-metadata-startup-20260910-123434.log`. Four server and four client reports independently parsed as passing, including the new diagnostic check. Exact per-process commands/logs/durations are retained in `build/mixin-metadata-startup.json`. Existing client resource checks pass; screenshots remain outside the repository. No new visual-parity claim.
- Extracted the actual guard expressions from both scripts and evaluated them against the prior Forge log and all eight new logs. Both reject the historical diagnostic and accept the new startup logs.
- Alternate Stonecutter state: bounded `./gradlew 'Set active project to 1.20.1-forge' --no-daemon`, `./gradlew build --no-daemon`, and `./gradlew 'Reset active project' --no-daemon`; all exit 0 (6s/32s/7s), logs `build/mixin-metadata-switch-20260910-123742.log`, `build/mixin-metadata-alternate-20260910-123748.log`, `build/mixin-metadata-reset-20260910-123820.log`. All four artifact pairs pass in the alternate state.
- Round-trip snapshot `/tmp/arcane-mixin-metadata-roundtrip-20260910-123742.tar`: restored the four known inactive-comment normalization changes in ContentRegistry, RadiantTankRenderer, AmphoraFluidStorage and RadiantTankStorage. Independently compared all 668 source/controller files byte-for-byte after restoration. Automatic switching is not byte-stable.
- Final canonical `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 30s, `build/mixin-metadata-final-20260910-123925.log`; all four artifact pairs and whitespace checks pass.
- Final XML (tests/failures/errors/skips): Fabric 1.20.1 `196/0/0/0`, Forge standalone `36/0/0/0`, Forge native runtime `193/0/0/0`, Fabric 1.21.1 `196/0/0/0`, NeoForge `273/0/0/0`. Existing suites, not new gameplay cases; Forge standalone overlaps runtime coverage.
- All test clients/servers stopped; no project game JVM remains. Unrelated processes were left untouched.

This closes the specific Mixin metadata diagnostic recorded in `CHEST_NATIVE_ACCEPTANCE.md`. Full feature migration, connected-player interaction/rendering and multiplayer acceptance remain incomplete. The Radiant Furnace exclusion remains unchanged.
