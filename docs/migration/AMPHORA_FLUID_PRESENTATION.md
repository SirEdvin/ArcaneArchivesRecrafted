# Amphora fluid presentation — remaining implementation contract

Status: dynamic fluid layer implemented; visual acceptance deferred to the user's initial test.

## Existing port

- `client/AmphoraClient.java` selects unlinked/filling/draining item models through the registered `amphora_state` predicate.
- The filling/draining models use ordinary `minecraft:item/generated` for their base layer. `AmphoraRenderer` now adds the masked fluid pass through the existing transformed item-render hook, independently of that base model.
- All five required textures already exist under `assets/arcanearchives/textures/item/`: base, tilted base, unlinked base, filling mask and draining mask. Do not duplicate/re-import them merely to restore rendering.
- `RadiantAmphoraItem.appendLinkedTooltip` reads the supplied world only, checks matching dimension and chunk availability, and uses native Fabric fluid-variant or Forge-family FluidStack display names for a nonempty local linked Tank.

## Pinned upstream model contract

At `bb99accf48ed583e29b0efae56e28c963407b8df`, resources under `assets/arcanearchives/blockstates/` establish:

- `radiant_amphora_fill.json`: `forge:forgebucket`, upright base, filling fluid mask, default item transform, default fluid `water`, `flipGas: false`.
- `radiant_amphora_empty.json`: the same dynamic bucket model but tilted base and draining mask, again water default and gas flipping disabled.

Runtime follow-up: upstream `RadiantAmphoraItem.FluidTankWrapper` forwards fluid properties/drain simulation to the linked Tank. Forge 1.12.x `ModelDynBucket.BakedDynBucketOverrideHandler.handleItemState` calls `FluidUtil.getFluidContained` and returns the original water-configured model when null. Its fluid-layer bake draws masked NORTH/SOUTH faces at `7.498/16` and `8.502/16`; `flipGas: false` prevents inversion. Reference: https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/client/model/ModelDynBucket.java .

The port uses loader-native still sprites/tints, existing mode masks and those face depths, reading only an available linked Tank in the supplied client world. Missing/empty/unavailable links use water presentation without claiming stored water or changing tooltips. Current atlas sprites are resolved each draw rather than retained across reloads. No item fluid capability, transfer simulation or server lookup is used for rendering.

Verification: four-target build/native regression suites passed in `build/amphora-fluid-layer-20260913-162008.log` (exit 0, 119 s). These checks establish compilation and existing native behavior, not actual GPU rendering, fluid appearance or connected updates. No client campaign was run, per user direction.

## Implementation boundaries

Use the existing external dependencies and loader-native rendering facilities. Keep linked fluid presentation read-only and client-local; never reach into an integrated server's live Tank or force-load a chunk to draw an item. Keep displayed fluid information separate from transfer authority and do not add a second authoritative fluid inventory to an Amphora.

The generic copied-item integration concern remains explicitly deferred under 0043. This presentation work does not reopen it.

## Delivery order

Per the user's latest direction, finish this implementation and the complete Tome before handing over the 0.0.2 build for the user's first test. Continue existing build/native/artifact checks, but defer new graphical acceptance tooling. No fluid-rendering completion, connected delivery or visual-parity claim follows from this source audit.
