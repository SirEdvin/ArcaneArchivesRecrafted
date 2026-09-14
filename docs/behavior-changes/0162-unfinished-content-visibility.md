# Unfinished content visibility

## Original behavior
Registered placeholders and incomplete book topics remain discoverable.

## Approved behavior and reason
Hide audited unfinished content from Tome, creative and EMI/JEI; preserve registry IDs and runtime state. Do not hide implemented gameplay solely because graphical verification is pending.

The hidden item set is `immanence_incubator`, `matrix_core`, `matrix_distillate`, `matrix_reservoir`, `celestial_lotus_engine`, `echoreverb`, `echoconform`, `verdant_censer`, `spellbook_library`, `obstruction_charm`, `serenity_charm`, and `matrix_brace`. The first, chambers, censer, library and charms are placeholders; matrix/lotus blocks have partial state/placement/crafting plumbing but no complete reachable production/network behavior. The brace only feeds these unfinished machines. Existing implementation and save IDs remain intact. Keep `material_interface` and `containment_field`: finished Trove/Tank recipes use them.

The Tome additionally hides the conceptual `StorageMatrix` and `RepositoryMatrix` topics without registered usable blocks. Recipe ingredient icons and useful finished gem prose remain. Strip source PLACEHOLDER labels from Echoes/Empowered Quartz, which are real usable crafting ingredients. Chromatic powder and Arsenal-off exclusions remain independent.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Shared visibility/resource tests and artifact checks pass; exact evidence and deferred viewer-combination acceptance are recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
