# 0093 — Debug Orb privacy and destructive-action access

Status: approved by the user (“Yes, I approve it”); registered storage actions implemented and assembly/package/fixtures verified. Full diagnostics and runtime acceptance remain incomplete. No external upload performed or implemented.
Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Original behavior

Inspected master baseline 80944ce45c6559243d8928cc4b305bf379388652. ItemRegistry.java:115 registers DEBUG_ORB; this is not a dormant item.

items/DebugOrbItem.java:71–255 reports storage state on use. The report includes coordinates, network/tile UUIDs, Chest names, displayed/stored item identities and quantities. Both client and server paths call UploadUtils.uploadToHaste, then print the resulting URL. util/UploadUtils.java:19–40 POSTs to https://paste.dimdev.org/documents without an in-game confirmation or explicit timeouts.

DebugOrbItem.java:83–96 sneak-use fills an empty Trove with snowballs or clears its contents. The item tooltip says creative-only, but this method does not enforce a creative/permission check. Original left-click Chest diagnostics are separate (269–300).

## Approved behavior

Preserve the registered item, source-backed diagnostics, and deliberate Trove fill/clear functionality. Require creative mode plus operator permission level 2 for destructive fill/clear; retain normal server authority and protection checks. Deliver diagnostics locally to the invoking player or an operator-readable local report, rather than automatically uploading world information to a third-party service. No automatic network upload from either physical side.

Reason: avoid silent external disclosure and destructive actions authorized merely by possessing a nominally creative-only item. The user explicitly approved these changes to the original behavior. Network-dependent report fields must use the real migrated network; do not fabricate IDs or claim full diagnostic parity before that integration exists.

## Verification

Implemented DebugOrbItem, DebugOrbEvents, item/creative registration, original name/tooltips, model, texture and animation sidecar (all three assets byte-identical to the inspected master baseline; MIT notice retained). Right-use reports Chest/Trove/Tank storage locally to the invoking player; left-click Chest inspection consumes the attack. Fabric callbacks run after the default phase; Forge-family attack listeners honor cancellation and explicit use denials. Forge-family early-use hooks retain reporting precedence over block menus.

Destructive sneak-use stays in native item useOn, behind player/access/distance/loaded-position checks, creative plus permission level 2, and the Trove's live server-storage checks. Empty Troves receive capacity-sized snowball contents; nonempty Troves are deliberately cleared, with reference state updated and upgrades retained. The fixture invokes only the internal storage operation, not a player interaction.

Diagnostics currently report authoritative server-side storage state only. Network IDs, network membership/routing, original client/server discrepancy reports and remaining original diagnostic fields are not complete; a visible message identifies the missing network diagnostics. No synthetic network state is generated.

`timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*DebugOrbTest' --offline --no-daemon`: exit 0, 29s, `build/debug-orb-20260909-205038.log`. NeoForge XML: 3 tests, zero skips/failures/errors. Coverage: all permission-policy combinations, capacity-sized fill/clear with retained upgrade, registered one-stack item/tooltips and read-only Chest report. These are policy/state fixtures, not connected-player or event-dispatch tests.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/debug-orb-artifacts.log`; all four production/source pairs pass, including class/source and original artwork/animation/atlas checks. `git diff --check` passes. No client/server launched. Native protection cancellation, actual main/offhand dispatch, rendering, persistence, multiplayer and dedicated-server runtime acceptance remain pending; no full gameplay parity claimed.
