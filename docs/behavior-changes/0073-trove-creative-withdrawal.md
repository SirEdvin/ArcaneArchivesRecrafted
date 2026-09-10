# 0073 — Trove creative withdrawal and scepter mining

Status: implemented and four-target assembly/artifact verified; runtime acceptance pending.

## Original behavior

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, `blocks/RadiantTrove.java:88–95,194–218,274–280`:

- Creative removal with a non-scepter main hand invokes the ordinary Trove attack/withdrawal route and returns false, keeping the Trove in the world. Scepters permit removal.
- Withdrawal only occurs on the top or the source's rotated front face, using the existing one-item sneak / native stack normal action and cooldown.
- Scepters mine at `5 / hardness / 30` rather than the ordinary tool speed.

The migrated Trove already implements normal attack withdrawal but lacked the creative removal interception and scepter mining-speed override. Native creative destruction skips `Block.attack`, so ordinary attacks could remove packed storage instead of withdrawing.

## Adaptation and approval

Restore these reachable behaviors. Forge/NeoForge use `onDestroyedByPlayer` after their native break authorization. Fabric uses an ordered `PlayerBlockBreakEvents.BEFORE` callback after default-phase protections. A client-only branch of Fabric's native attack callback returns SUCCESS (send the attack request without local destruction) for this case; it never extracts items. The server performs the withdrawal through the existing Trove access, direction and cooldown checks. Callbacks already canceled by default-phase listeners cannot reach this withdrawal. Other mods' custom event phase ordering still needs compatibility acceptance.

The original scepter formula applies to the original positive Trove hardness. If another mod makes the block unbreakable (negative hardness), retain native zero destroy progress instead of negative progress; this defensive boundary uses the user's delegated, reconfirmed safety-correction authority. No normal mining speed or capacity change is introduced.

Only currently registered scepters are recognized (`StorageScepterItem`). When Translocation is migrated its scepter identity must join the same check. No item whitelist by registry-name substring is accepted.

The separate sneak-scepter spill operation, generic item-form automation, multiplayer acceptance and parent storage migration remain unfinished. This restores creative non-removal, not full Trove parity. Approved packed-removal policy 0038 still applies to actual removals.

## Verification

`timeout --foreground 10m ./gradlew assemble --no-daemon` passed all four targets, exit 0, 21s, `build/trove-creative-20260909-164945.log`. `timeout --foreground 60s python3 scripts/verify_artifacts.py` passed all four production/source pairs, exit 0, `build/trove-creative-artifacts.log`. `git diff --check` passed.

The pinned Forge 47.3.39 mapped client/server sources confirm `onDestroyedByPlayer` is invoked on both sides and after server block-break authorization. NeoForge 21.1.234's `IBlockExtension` confirms the same override signature and native removal responsibility. Fabric's event source confirms BEFORE short-circuits on cancellation and client AttackBlock SUCCESS sends the request while stopping local processing. Mapped 1.21.1 `BlockBehaviour` exposes the overridden destroy-progress signature. These are API/control-flow checks, not runtime gameplay results.

Connected-player creative interaction, survival/scepter removal, save/restart and mod-protection compatibility remain deferred gameplay acceptance. No new test campaign, client/server run or parent completion claim.
