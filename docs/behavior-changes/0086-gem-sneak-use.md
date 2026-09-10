# 0086 — Gem-specific sneak-use dispatch

Status: implementation and four-target assembly/package verified; connected runtime acceptance remains open.

Original behavior: baseline `80944ce45c6559243d8928cc4b305bf379388652`, items/gems/** doesSneakBypassUse overrides return true for Agegleam, Switchgleam, Salvegleam, Cleansegleam, Rivertear, Parchtear, Mountaintear and Phoenixway. They explicitly return false for Murdergleam, Slaughtergleam, Stormway, Mindspindle, Elixirspindle, Munchstone and Orderstone. Transferstone also declares false upstream, but is not implemented/enabled by this change.

Restore those constant choices in the existing common gem base; native Forge/NeoForge hooks delegate to that policy. Extend the existing Fabric two-hand bypass adapter rather than adding competing injections. Empty hands and scepters retain their existing behavior. Mixed eligible gem/scepter pairs bypass; an ordinary item or non-bypassing gem in either hand prevents bypass. Charge, toggle and upgrade state do not change the original constant result. Do not change player sneak state, permission checks, charge consumption, or add new abilities.

Affected targets: all four supported leaves. Approval: source-backed restoration under standing migration approval, no gameplay deviation proposed. Registered gems remain subject to the existing Arsenal configuration.

Verification:
- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*GemSneakUseTest' --tests '*TranslocationScepterTest' --offline --no-daemon`: exit 0, 28s, `build/gem-sneak-20260909-183535.log`. All four targets assembled.
- NeoForge XML: 5 tests, 0 skipped/failures/errors (2 gem fixtures and 3 existing scepter fixtures). Tests cover original true/false choices for every migrated gem, state-independent read-only hooks, and all ordered pairs of gems, scepters, empty hand and ordinary stone against native two-hand hooks.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/gem-sneak-artifacts.log`; all four production/source pairs pass. `git diff --check` passes.

Native hook fixtures and assembly do not establish connected interaction, protection-mod compatibility or actual Fabric mixin application. Those remain runtime acceptance tasks. No development clients or servers were launched.
