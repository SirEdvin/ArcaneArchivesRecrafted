# 0146 — Brazier automation without the unsafe direct setter

Approval status: approved by the user ("yes"). Forge/NeoForge non-modifiable capabilities implemented; Fabric transactional adapter remains pending.

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `tileentities/BrazierTileEntity.java:120–128,404–448`, exposes an `IItemHandlerModifiable` on every side. Its 999 virtual slots are always empty and cannot be extracted. Ordinary `insertItem` routes the offered stack and returns the unpaid remainder to its caller. By contrast, `setStackInSlot` routes immediately, ejects leftovers, and returns void. Rejected spawning at lines 166–176 ignores the spawn result.

The direct setter therefore has no explicit source-recovery contract when ejection fails. This is source evidence, not a reproduced live automation loss. It is not a concurrency issue.

## Proposed behavior

Expose the Forge/NeoForge Brazier capability as `IItemHandler`, not `IItemHandlerModifiable`. Preserve ordinary insertion, exact remainders, simulation, all-side exposure, empty reads/extraction, and the original virtual-slot count/limits. Do not expose `setStackInSlot` as a deposit command.

Fabric uses its native insertion/extraction transaction contract and does not gain an equivalent direct setter. Its adapter still requires verified destination rollback on transaction abort.

Callers must use insertion and pay only the accepted amount. Do not replace the omitted setter with a throwing setter, silently discard an offered stack, introduce a device input/recovery buffer, or reuse player-owned pending returns for an automation source.

## Reason

Keep the automation source responsible for its unpaid items through the normal remainder/transaction contract. Approved 0144 and 0145 recover existing entity and player sources respectively; neither supplies an automation-owned recovery destination. Omitting the modifiable interface avoids new persistent device buffering and a second exceptional ownership policy.

## Compatibility impact

Ordinary callers using `IItemHandler.insertItem`, including normal hopper-style insertion, retain the intended API. A caller specifically requiring `IItemHandlerModifiable` or relying on the upstream direct setter would no longer have that interface. No claim is made that all third-party consumers have been audited or that none use the setter.

## Affected targets

Minecraft 1.20.1 Forge and 1.21.1 NeoForge: native capability interface change from the upstream contract. Fabric 1.20.1/1.21.1: native transactional insertion remains required; no new direct-setter equivalent.

## Verification required

Native capability lookup from every side and unsided; capability not modifiable; exact full/partial/refused remainders; simulation and caller payment; empty extraction; slot contract; native automation and Fabric commit/abort across destinations. Preserve player/entity deposit regressions and artifact isolation across all four targets.

## Implemented evidence

`BrazierItemAutomation` exposes only `IItemHandler` on Forge/NeoForge, with 999 empty virtual slots and slot limits, no extraction/setter/buffer, exact routing remainders and simulation. The shared device entry point attempts gated sound only after complete nonempty real insertion. Native Forge/NeoForge fixtures retrieve every sided and unsided capability, reject the modifiable interface, and verify full/partial/zero acceptance, repeated simulation, explicit caller remainder payment, independent remainders, and ownerless/detached/removed rejection. Shared adapter assertions also run in both Fabric native suites, but Fabric has no native automation registration yet.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 78 s, `build/brazier-automation-final-20260913-114403.log`; all four native suites passed. Initial fixture compilation failed because Brazier ownership uses `recordPlacer`, not Chest's `setOwner`; corrected to the existing placement API. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, `build/brazier-automation-artifacts.log`.

Native hopper source payment is now verified on Forge/NeoForge through manually advanced native hopper ticks: full/partial/zero acceptance retains exactly the unpaid source, including repeated refused attempts. `build/brazier-hopper-20260913-114732.log`: four-target build/native suites, exit 0, 56 s. Automatic world-loop scheduling, audible sound acceptance, broader capability lifecycle transitions, and Fabric transactional commit/abort remain unverified or unimplemented; these results do not establish full automation parity.
