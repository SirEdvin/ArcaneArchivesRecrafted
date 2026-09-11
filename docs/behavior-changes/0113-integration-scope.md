# Integration scope revision

## Original behavior

Upstream includes Crafting Tweaks, CraftTweaker recipe scripting, JEI, Astral Sorcery Lightwell hooks and Thaumcraft integration. The migration backlog previously retained these for compatibility evaluation.

## Approved scope

- Exclude Crafting Tweaks and CraftTweaker integration; implement KubeJS recipe scripting integration instead.
- Exclude Astral Sorcery and Thaumcraft integrations, including Lightwell hooks. Do not invent replacement magic-mod mechanics.
- Implement EMI integration alongside JEI, covering the relevant recipe display, transfer and hover workflows supported by each verified API.
- Integrate Jade only for overlays; exclude HWYLA and The One Probe integrations.
- JEI, EMI, KubeJS and Jade are optional player dependencies. Add verified development/API dependencies and activate integrations only when installed; do not bundle them or require players to install them. Verify startup without these mods.
- Omit KubeJS integration on 1.21.1 Fabric only; retain that Minecraft/loader target. KubeJS integration remains in scope on 1.20.1 Fabric/Forge and 1.21.1 NeoForge.
- Keep the existing Patchouli and optional Curios/Trinkets decisions.

## Reason and approval

Explicitly requested and approved by the user when revising the remaining-migration list. The user identifies Astral Sorcery and Thaumcraft as unavailable for the target Minecraft versions; exclusion is a scope decision, not a claim of independently verified release availability.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge, with the explicit KubeJS exception above. The user authorized official release-metadata retrieval and dependency additions, confirmed optional player dependencies, and selected Jade only. Verify exact releases and APIs per leaf before implementation. Any additional unavailable target requires a user decision rather than silent substitution or reduced coverage.

## Verification

### Arsenal viewer-index visibility restored

Both optional viewers share the pinned upstream exclusion list: disabled Arsenal gems and always-hidden powders, with no recipe or server changes. Four-target build/native suites, three registered-item tests and all artifact pairs pass. Four actual Fabric 1.21.1 viewer/configuration sessions verify disabled/enabled index behavior and clean shutdown; see [visibility evidence and remaining scope](../migration/VIEWER_ITEM_VISIBILITY.md). Existing 0130 exclusions remain unchanged.

### Resonator production information restored

JEI's original quartz-production page and its EMI counterpart are implemented with the original artwork, configured-minute caption, output lookup and workstation. No machine behavior, recipe payment, transfer or grant path changed. Four-leaf build/native suites and artifact contracts pass; both actual Fabric 1.21.1 viewer screens were verified separately. See [source, diagnostics and runtime evidence](../migration/RESONATOR_VIEWERS.md), including the local-versus-remote configuration boundary. Broader integration acceptance remains open.

### Combined Fabric 1.21.1 compatibility issue

The subsequent combined JEI/EMI client passed occupied-grid transfer, paid output and missing-ingredient refusal, but exposed duplicate bridged tag-recipe IDs. Under [0130](0130-jei-emi-tag-recipe-collisions.md), the user explicitly chose to ignore this known limitation: repair is excluded, with current pins, both viewers, tag categories and diagnostics retained. No approval blocker remains. Process success is not full coexistence acceptance; broader integration checks remain separate.

### Radiant Crafting Table EMI counterpart

EMI now uses its standard crafting handler with grid/player ingredient sources, grid-only fill destinations, a separately exposed native output and excluded bookmark ghosts. All four build/native suites pass (`build/emi-transfer-labels-20260911-131318.log`, exit 0, 78s) and artifact pairs pass. A real EMI-only Fabric 1.21.1 client verifies four-Plank transfer, one paid Crafting Table, missing-ingredient refusal and unrelated-inventory conservation; a second process verifies saved state and resolved ingredient-tag label warnings. See [EMI evidence](../migration/RADIANT_CRAFTING_EMI.md). Other connected leaves, combined viewers and broader acceptance remain open; Gem Cutter still has no transfer handler.

### Radiant Crafting Table JEI transfer restored

The existing upstream transfer/catalyst behavior is now implemented through JEI's standard crafting-grid handler, with output/bookmark slots excluded and no custom transfer protocol. All four build/native suites and artifact pairs pass (`build/jei-transfer-20260911-124933.log`, exit 0, 233s; `build/jei-transfer-artifacts.log`). A real JEI-enabled Fabric 1.21.1 client passed ingredient transfer, native paid output pickup, missing-ingredient refusal and unrelated-inventory conservation (`build/jei-transfer-live-1789131245043438857.log`, exit 0, 243.28s). See [source/API/runtime evidence](../migration/RADIANT_CRAFTING_JEI.md). Other connected leaves and combined-viewer acceptance remain open; this does not add a Gem Cutter transfer handler.

### KubeJS compatibility prerequisite (current NeoForge JEI pin)

Inspection of the actual pinned KubeJS JARs found required Rhino `>=2001.2.2-build.1` and Architectury `>=9.1.12` on both 1.20.1 leaves. The 1.21.1 NeoForge JAR requires NeoForge `>=21.1.199` and Rhino `>=2101.2.7-build.81`; when JEI is installed on the client it additionally requires JEI `>=19.25.0.322`. Thus the earlier `19.21.2.313` JEI startup fix below was insufficient for the eventual combined KubeJS profile.

Under the approved older-JEI beta policy, NeoForge JEI is now `UJRXzDfp` / `19.25.1.334` (beta). Its unmodified manifest declares NeoForge `[21.0.118-beta,)`, retaining the same Minecraft range caveat as the earlier candidate. Downloaded and Gradle-resolved SHA-512 match official release metadata and the updated artifact record. Platform pins remain unchanged; no loader validation override was added.

The Jade+JEI+EMI NeoForge client and dedicated server were rerun with this pin: exit 0, all smoke assertions passed, 26.20s and 17.54s respectively. Logs: `build/smoke-client-1.21.1-neoforge-1789067222558912178.log` and `build/smoke-server-1.21.1-neoforge-20260910-190728-795249.log`. The client retained only the observed headless OpenAL error in its error/exception scan; the server scan was empty. **KubeJS itself was not installed in these runs**: companion resolution, scripting implementation and actual combined runtime acceptance remain pending. Older pin-specific startup evidence below remains historical.

### Installed client matrix

Jade+JEI+EMI development client startup now passes on all four leaves. NeoForge initially failed its startup assertions despite process exit 0: JEI `19.51.0.418` required NeoForge `21.1.238`, above our unchanged `21.1.234`. Following the approved older-JEI policy, its pin is now `zRGLFYRx` / `19.21.2.313` (**beta**); downloaded and Gradle-resolved hashes match the artifact record. Its manifest declares NeoForge `[21.0.118-beta,)`; the Minecraft range is `[1.21, 1.21.1)` despite the release's 1.21.1 label. Actual unmodified loader startup accepted this artifact; no dependency override was added. Do not infer broader runtime parity from metadata or this smoke.

Passing client logs and durations: 1.20.1 Fabric `build/smoke-client-1.20.1-fabric-1789066143731879185.log` (30.70s); 1.20.1 Forge `build/smoke-client-1.20.1-forge-1789066281220026567.log` (26.20s); 1.21.1 Fabric `build/smoke-client-1.21.1-fabric-1789066307503188477.log` (23.19s); 1.21.1 NeoForge `build/smoke-client-1.21.1-neoforge-1789066590415465498.log` (24.69s). Each uses the installed-profile command below and exits 0 with all harness checks true. Earlier NeoForge diagnostic scanning caught Jade's missing `config.jade.plugin_arcanearchives.storage_overlay` translation despite passing smoke assertions; an English fallback was added and a fresh NeoForge run confirmed that exception absent. Headless audio errors remain. Earlier client results on other leaves predate this additive translation fix.

Final default four-leaf build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 47s, `build/installed-matrix-final-build.log`. These are startup checks, not connected recipe/overlay acceptance or proof of all optional combinations. Subsequent combined Jade+JEI+EMI dedicated-server bootstrap passed on all four leaves (exit 0, 81s total), including explicit Jade server-plugin discovery, clean save/stop and no remaining project JVMs; see [runtime evidence](../migration/OPTIONAL_INTEGRATION_STARTUP.md). Viewer transfer/hover/reload, connected overlays, other optional combinations and KubeJS remain open.

### Installed-profile compatibility investigation (1.20.1 Fabric blocker resolved)

The user subsequently approved using an older JEI version in response to the beta-approval question. Only the 1.20.1 Fabric pin changed to `jJOr2rUn` / `15.20.0.134` (Modrinth **beta**, not stable). Its actual metadata requires Fabric Loader `>=0.16.3`, Fabric API `>=0.92.2+1.20.1`, and Java `>=17`; existing platform pins remain unchanged. Downloaded and Gradle-resolved SHA-512 hashes match the updated artifact record.

The installed Jade+JEI+EMI client smoke now passes on 1.20.1 Fabric: the same installed-profile command below exited 0 in 30.70 seconds, with all startup/close checks true. Full log: `build/smoke-client-1.20.1-fabric-1789066143731879185.log`; loader inventory explicitly lists all three integrations and JEI `15.20.0.134`. Headless OpenAL errors remain; connected recipes/overlays are not verified. Default four-leaf `timeout --foreground 10m ./gradlew build --no-daemon` passed in 34 seconds, log `build/jei-older-build-20260910-184940.log`. Artifact verification output: `build/jei-older-artifacts.log`. Other JEI pins are unchanged. Historical blocker investigation follows; it no longer requires user input for this downgrade.

The opt-in development property `-PoptionalIntegrationMods=jade,jei,emi` now adds only those selected libraries to the loader-correct runtime configuration; the default remains empty. KubeJS is rejected by this property until its runtime companions are configured. Nothing is bundled or added to mandatory player metadata.

The first installed-profile client test failed before mod initialization on 1.20.1 Fabric: pinned JEI `YRfUnbXb` / `15.56.0.205` requires Fabric Loader `>=0.19.4` and Fabric API `>=0.92.11+1.20.1`, while the project uses `0.17.2` and `0.92.6+1.20.1`. Both Fabric Loader's diagnostics and the actual JEI JAR's `fabric.mod.json` confirm these requirements. Prior game-version/loader release filtering and API compilation did not validate these transitive runtime minima.

Command: `ORG_GRADLE_PROJECT_optionalIntegrationMods=jade,jei,emi timeout --foreground 11m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py 1.20.1-fabric`. Gradle exited 1 after 22.02 seconds; full log: `build/smoke-client-1.20.1-fabric-1789064937097068119.log`. No project Java processes remained. Other installed-profile leaves were not run after this failure. `git diff --check` passed; the new profile is not yet matrix-validated.

The user approved selecting an older stable JEI without upgrading the platform. However, both the filtered and full official Modrinth version responses list only two `release` entries for 1.20.1 Fabric: `YRfUnbXb` / `15.56.0.205` and `px4ZUY8R` / `15.49.0.200`. The older artifact was downloaded and its SHA-512 matched the published value `cd4994dd09ed4c47e145bf1c1cf9806d3ccedf3d09e8f205c20b4bae1fcfd799a916ab6d2396d5dba0f63bd1a1131353d0aa54f7bb3d378f79e024ced73039a3`. Its `fabric.mod.json` still requires Fabric Loader `>=0.19.4` and Fabric API `>=0.92.11+1.20.1`; therefore it cannot resolve this blocker. Metadata: `https://api.modrinth.com/v2/version/px4ZUY8R`.

No compatible stable release was found on the configured Modrinth source. The other 189 matching versions are labeled `beta`; do not silently treat those as stable. Dependency pins remain unchanged. Further user approval is needed to evaluate a compatible beta or investigate a platform upgrade. This result is specific to Modrinth release metadata, not proof that no other official distribution could label an older build stable.

Post-provider optional-absence startup now passes on all four development clients and servers; see [the runtime report](../migration/OPTIONAL_INTEGRATION_STARTUP.md). Headless audio initialization errors are explicitly recorded, not treated as clean logs. Installed-integration acceptance remains open. The dependency-stage pending statements below describe their historical checkpoints.

JEI and EMI compile-only dependencies are pinned for all four leaves; KubeJS is pinned for the three approved leaves and absent from 1.21.1 Fabric. Stable release metadata and SHA-512 hashes of the actual Gradle-resolved JARs are recorded in `../migration/optional-integration-artifacts.json` (11 verified artifacts). No library is bundled or declared mandatory for players. JEI/EMI publish MIT licensing; KubeJS publishes LGPL-3.0-only licensing; no upstream implementation was copied. Required KubeJS runtime companions remain recorded in the metadata and must be installed/resolved before runtime integration testing; compile-only resolution is not a runtime installation.

Dependency matrix build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 63 seconds, `build/integration-dependencies-20260910-180206.log`. Production/source artifact verification passes all four targets. Recipe providers, scripting adapters and installed-mod compatibility acceptance remain pending.

Jade compile-only dependencies are now pinned by Modrinth release ID in all four leaves: 1.20.1 Fabric `ahavHbAT` (11.13.3+fabric), Forge `xJQHCmWJ` (11.13.3+forge); 1.21.1 Fabric `adpNvTZS` (15.10.6+fabric), NeoForge `eYz2YBGT` (15.10.6+neoforge). Version/loader metadata was retrieved from `https://api.modrinth.com/v2/project/jade/version`. They are not bundled or mandatory player dependencies. `timeout --foreground 10m ./gradlew build --no-daemon` passed (exit 0, 59 seconds), log `build/jade-dependency-20260910-175146.log`. This verifies dependency resolution and the canonical build, not Jade overlays or connected runtime compatibility. Provider implementation, installed-Jade runtime testing and other integration dependencies remain pending.

The subsequent Jade provider implements the original HWYLA chest-name, Trove count/item-or-empty, and Resonator progress/status lines using server-generated components. Fabric uses the optional `jade` entrypoint; Forge/NeoForge use Jade annotation discovery. No normal mod entrypoint references the optional API. The provider only reads existing state and sends display lines, not full inventories or ownership NBT. The original providers were inspected under `integration/hwyla/providers/` in the pinned reference checkout.

Provider build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 56 seconds, `build/jade-provider-20260910-175746.log`. `scripts/verify_artifacts.py` passes all four production/source pairs after explicitly accounting for the two new provider classes. Installed-Jade discovery, connected overlays and runtime compatibility remain unverified; the migration integration checkbox stays open.

Future acceptance must cover optional-mod absence, supported combinations, counted recipes, transfer authorization/conservation, and KubeJS add/remove/reload semantics. Arbitrary NBT/component recipe scripting is explicitly excluded by the subsequent user decision [0118](0118-scripted-stack-data.md); existing creator stamping and inventory data conservation remain required. Historical source inventories and feature reports remain provenance; their CraftTweaker/Crafting Tweaks/Astral Sorcery/Thaumcraft pending notes are superseded by this decision.
