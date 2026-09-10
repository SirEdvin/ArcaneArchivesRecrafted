# Craftable storage components

Material Interface, Containment Field and Matrix Brace now have shared item registration, creative entries, original gold tooltips, English/Portuguese names, original models/textures and paid Gem Cutter recipes on all four targets.

Pinned source: `release/0.2.0.25-mixins8`, `bb99accf48ed583e29b0efae56e28c963407b8df`, upstream `items/MaterialInterfaceItem.java`, `items/ContainmentFieldItem.java`, `init/ItemRegistry.java` and `init/RecipeLibrary.java`. These are registered reachable items, not dormant classes. The upstream MIT notice remains packaged.

| Output (one) | Original Gem Cutter cost |
| --- | --- |
| `material_interface` | One Scintillating Inlay, one gold ingot, one Shaped Radiant Quartz |
| `containment_field` | One Scintillating Inlay, two gold ingots, two Shaped Radiant Quartz |
| `matrix_brace` | One Scintillating Inlay, two gold ingots |

Gold uses the existing native tag bridge under decision 0027. No substitute recipe or changed cost was introduced. All three outputs use the paid menu path in 0028; native stack limits remain unchanged. One shared `StorageComponentItem` supplies the concrete tooltip keys without introducing a new upgrade framework.

The five original model/texture/animation files were copied byte-identically from the pinned Git objects. Their SHA-256 values are checked in `scripts/verify_quartz_resources.py`; both legacy `textures/items` sprites have explicit atlas entries. Containment Field retains interpolated frames 0–7 at frametime 5. Material Interface has no animation metadata in this release. Portuguese item names are preserved; the upstream Portuguese tooltips are English and remain so rather than being silently replaced.

## Remaining scope, not approved removals

The same pinned release's `items/MatrixBraceItem.java`, registry and recipe library establish Matrix Brace as reachable, with SIZE upgrade size 2, slot 0, for both Radiant Tank and Radiant Trove. Installation remains part of the missing device port. Its model and static PNG are copied byte-identically and hash-checked; there is no animation metadata in this release. Its original English/Portuguese names are Matrix Brace / Suporte da Matriz; the supplied Portuguese tooltip is English, retained as with the other components. No new behavior deviation or dependency was introduced by this craftable-item addition.

This is the component/acquisition part of the storage port, not functioning storage upgrades. Upstream Material Interface targets Radiant Trove and Containment Field targets Radiant Tank; both declare SIZE, size 3, upgrade slot 1 and sneak-use bypass. Tank/Trove devices, upgrade installation, sneak-use bypass and original advancement integration remain unfinished. The original tooltips describe that upstream role, not verification that the missing devices work. Upstream assets and role information are retained for those implementations. Raw-quartz acquisition and broader initial progression also remain open.

## Verification

Latest component follow-up: `build/matrix-brace-20260908-113600.log`, exit 0 in 31s; 157 tests per Fabric leaf, 161 on NeoForge, zero failures/errors/skips; Forge test compilation only. The existing registered-progression test now also checks insufficient gold, exact Inlay/two-gold payment with no shaped-quartz requirement, whole shift-delivery and no unpaid repeat for Matrix Brace. Four artifact/resource checks and server registration/reload/full-stack/save/stop fixtures pass (`build/matrix-brace-servers-20260908-113654.log`, exit 0). Consolidated report: `build/matrix-brace-verification.json`. No visual or connected-player/upgrade-installation acceptance is claimed. Earlier verification below is historical.

`build/storage-components-20260908-105910.log`: standard scoped matrix exits 0 in 30s. Fabric 1.20.1 and 1.21.1 each pass 157 tests; NeoForge 1.21.1 passes 158, all with zero failures/errors/skips. Forge compiles test sources but does not execute this suite.

The loader-aware `registeredProgressionRecipesDeliverRegisteredOutputsUsingRealLoaderLookup` test now crafts actual registered Inlay and both components. It checks missing-gold rejection, exact table/main-player costs, complete shift-delivery, and refusal to craft again without payment. This is a native-menu fixture, not a connected player or progression walkthrough. Fabric transaction fixtures retain the explicitly documented loader-lookup seam from 0028.

All four artifact/resource contracts pass. Four dedicated servers load/reload and verify 64-item stacks of both new registered components in the isolated fixture chest, then clean fixtures and save/stop. `build/storage-components-servers-20260908-110017.log` exits 0; per-server reports and test totals are consolidated in `build/storage-components-verification.json`. No clients or visual checks were run. Rendering, actual player interaction and storage-device behavior remain unverified.
