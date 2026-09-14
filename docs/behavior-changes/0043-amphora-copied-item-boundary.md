# Amphora copied-item fluid boundary

Status: deferred by explicit user direction to ignore this potential issue for now. Investigation is not a blocker for 0.0.2 or the remaining network/Tome implementation. Generic compatibility remains unfinished on all targets; the issue is neither repaired nor proven safe. Explicit live player/Tank/world/dispenser paths remain implemented; this deferral does not register a direct remote item proxy.

## Original behavior

The pinned Amphora's `FluidTankWrapper` delegates fill/drain from every copied ItemStack to the linked remote Tank. Original source: release `bb99accf48ed583e29b0efae56e28c963407b8df`, `items/RadiantAmphoraItem.java`. The item itself has no independent fluid contents.

## Concrete native incompatibility

Inspected Forge 47.3.39 and NeoForge 21.1.234 fluid utility sources. `FluidUtil.tryFillContainer(..., doFill=false)` makes a copy of the item, then calls its handler's `fill(..., EXECUTE)` to produce the simulated resulting item. The helper assumes copied items own independent fluid storage. A direct remote proxy would mutate the real Tank during simulation and could duplicate fluid without draining the source.

Fabric transactions do not solve the copied-container assumption by themselves. Current `recipe/gct/GemCutterFluidRemainders.java` prepares a copied input in a detached SimpleContainer/ContainerItemContext and commits its local fluid extraction before actual crafting payment. Registering the proposed remote item proxy would let that preparation drain the real Tank despite the detached input. This is source-traced evidence, not a newly run reproduction/test campaign.

## Current boundary and remaining design

Do not expose an unrestricted proxy while leaving these real consumers unsafe. No generic item capability/FluidStorage.ITEM binding is registered for the Amphora in the current code. `AmphoraFluidStorage` performs explicit live operations instead; placed Tank interaction bypasses copied-container helpers. Neither the original requirement nor eventual compatibility acceptance is closed by this temporary boundary.

A complete integration must distinguish committed live transfer from detached item preparation without relying on guessed caller intent, stack traces or catching runtime exceptions. It must conserve the remote Tank across copied-item simulations, recipe previews/payment failure, container stowing, multiple Amphoras and third-party native fluid consumers. Do not solve this by caching a second authoritative copy of the remote fluid in each Amphora.

Reason: implementing a known duplication/loss path is not acceptable migration parity. A safe native integration remains on the implementation queue; its eventual behavior decision/approval and gameplay verification must be recorded before claiming completion.

Current explicit-path integration compiles on all four targets; `build/amphora-final-20260908-191503.log`, exit 0, 16s. No simulation, multiplayer or runtime acceptance has been executed in this continuation.
