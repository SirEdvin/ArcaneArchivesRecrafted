# 0002 — Wearable integration

Status: approved API selection. No replacement installed or implemented.

Handoff: [HANDOFF.md](../HANDOFF.md). Do not re-ask whether Curios/Trinkets may replace Baubles; that API-family choice is approved. Do not interpret the choice as approval for different equipment slots, extra simultaneous effects, lost charges or required wearable dependencies.

Baseline: upstream `build.gradle` includes Baubles 1.12-1.5.2; `integration/baubles/BaubleBodyCapabilityHandler.java` and `BaubleGemUtil.java` implement equipment integration. Trace gem/charm callers before defining exact slot and activation behavior.

Proposed behavior: optional Curios integration for Forge/NeoForge and optional Trinkets integration for Fabric. Preserve the gem socket's body-equipment role, one selected active socket, charge synchronization and the existing non-wearable paths when the equipment mod is absent. Do not add a required equipment dependency or silently activate every equipped socket. Exact slot mapping and interactions with other mods must be checked before implementation; any unavoidable slot-semantic change needs explicit approval.

Targets: all four leaves.

Compatibility: slot availability, item identity, charge persistence, death handling and optional-mod requirements need explicit decisions.

Source evidence: `BaubleBodyCapabilityHandler.java:20` uses `BaubleType.BODY`; `BaubleGemUtil.java:15-24,35-46` selects the first matching body socket and synchronizes its slot. `GemUtil.java:430-453` keeps held-hand and open-socket-menu routes separate, using the optional Baubles route only when the socket menu is not open. `EventHandler.java:679-684` attaches the body capability only to the gem socket. Do not assume every charm uses this same equipment path.

Availability evidence: public Modrinth release metadata identifies Curios `5.14.1+1.20.1` (Forge), `9.5.1+1.21.1` (NeoForge), Trinkets `3.7.2` (1.20.1 Fabric) and `3.10.0` (1.21.1 Fabric). Exact IDs/dependencies/hashes are captured in `../migration/integration-candidates.json`. Curios reports LGPL-3.0-or-later; Trinkets reports MIT. No artifact has been installed or runtime-tested; preserve external dependencies and audit API licensing before copying anything.

Alternatives: maintain a local equipment interface or port another body-slot API. Neither has been designed or verified. Removing wearable functionality is not an implicit fallback.

Approval: the user explicitly replied “approve all three” to external Patchouli subject to licensing review, optional Curios/Trinkets, and leaving dormant features documented and disabled. Curios/Trinkets must remain optional and preserve existing non-wearable behavior. Incompatible slot semantics still require a separate decision; implementation and verification remain pending.

Verification: baseline slot/activation fixtures; equip/unequip, dimension travel, death and reconnect; optional-mod absence; no duplicate effects or lost charges.
