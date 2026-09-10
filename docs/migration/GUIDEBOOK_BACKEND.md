# Guidebook backend foundation

Current session entry point: [HANDOFF.md](../HANDOFF.md). This is the latest combined implementation/client/server evidence; the subsequent audit-only build and tome-route trace are in [TOME_PARITY.md](TOME_PARITY.md). Earlier foundation and quartz reports are historical slices. All commands below run from the repository root. Logs, downloaded binaries and snapshots are local-only and may need regeneration on another machine.

## Implemented and approved scope

The user approved external Patchouli subject to licensing review. All four builds now resolve a pinned external Patchouli dependency, expose its public API for later content integration, and require the exact tested version in loader metadata on both sides. No backend code, artwork or nested JAR is copied into this mod. `verify_artifacts.py` checks required dependency versions, Forge/NeoForge load order, and absence of bundled classes/JARs.

| Target | Maven version |
|---|---|
| 1.20.1 Fabric | 1.20.1-85-FABRIC |
| 1.20.1 Forge | 1.20.1-85-FORGE |
| 1.21.1 Fabric | 1.21.1-93-FABRIC |
| 1.21.1 NeoForge | 1.21.1-93-NEOFORGE |

Fabric uses `modImplementation` and the upstream Fiber repository for its transitive dependency. Legacy Forge uses ModDevGradle's SRG-remapping `modCompileOnly` API classifier and `modRuntimeOnly` implementation; NeoForge uses ordinary `compileOnly` API and `runtimeOnly` implementation dependencies. No `include`, shading or Jar-in-Jar configuration was added. The existing Minecraft/loader pins are unchanged.

**This is not the Tome of Arcana content port.** There is no replacement tome item, acquisition recipe, page conversion, opening handler or progression gate yet. The upstream prototype Patchouli book is not imported. Curios/Trinkets integration remains approved but unimplemented.

## Licensing and artifact provenance

Reviewed the publisher's `README.md` License Information and `LICENSE` at the actual release source commits:

- 1.20.1-85: `d20a6eb8fd4b2fc50a61e0872b6025e42e918767` — https://github.com/VazkiiMods/Patchouli/tree/d20a6eb8fd4b2fc50a61e0872b6025e42e918767
- 1.21.1-93: `0c2fdffc7e611a828007cecae7bfc3d7b312040a` — https://github.com/VazkiiMods/Patchouli/tree/0c2fdffc7e611a828007cecae7bfc3d7b312040a

Both declare CC-BY-NC-SA 3.0 for original code/assets. The publisher explicitly recommends normal external dependencies/API consumption rather than bundling and discusses Mojang mapping concerns for Jar-in-Jar. This implementation follows that external route, rather than relicensing or incorporating their work into our MIT sources. This does not authorize redistribution, commercial bundling, or omission of third-party obligations; release packaging still requires review.

`integration-candidates.json` retains the earlier Modrinth metadata snapshot. All four downloaded Modrinth binaries matched their recorded SHA-512 values. However, comparing those to the actually resolved Maven binaries exposed a provenance distinction: both 1.20.1 variants contain 14 different class entries, while both 1.21.1 variants have identical ZIP-entry contents. A sampled 1.20.1 `ItemStackUtil` disassembly differs in string-concatenation bytecode; complete semantic equivalence was not established. Do not substitute Modrinth hashes for Maven artifacts or claim those distribution binaries were exercised.

`patchouli-resolved-artifacts.json` records the actual Maven coordinates, URLs, SHA-512 values and differing ZIP entries. Each cached runtime JAR was also compared to a fresh download from its exact publisher Maven URL. The runtime evidence below applies to those Maven binaries.

## Original guide inventory

`guidebook-baseline.json` is generated from `git show` at the pinned Arcane Archives baseline, not from potentially edited checkout files. Reproduce with:

```sh
python3 scripts/audit_guidebook.py /path/to/upstream
python3 scripts/audit_guidebook.py /path/to/upstream --check
python3 scripts/test_audit_guidebook.py
```

The tome contains 7 chapters, 98 XML sections, 44 recipe elements, 256 link elements and 71 image elements. Sections are not rendered page counts. The inventory retains source hashes, literal identifiers, section positions/titles, condition definitions and ancestry, stack links, recipe XML, image attributes and includes. The subsequent [tome audit slice](TOME_PARITY.md) adds full section bodies, all 456 stack occurrences, two local template bodies and four included external standard-template bodies with pinned archive provenance. Six Python fixtures now cover these contracts. Parsed XML is not byte-identical source text; hashes retain original-byte provenance. No broken-looking source reference is silently corrected, and static inventory does not establish runtime reachability.

Remaining conversion must preserve the real tome's content, conditional text, custom Gem Cutter recipes, stack links and acquisition/opening behavior. Dormant source content must stay documented rather than becoming a newly enabled feature. Existing registries and recipes are incomplete, so displaying every source stack today would not constitute a faithful book port.

## Execution evidence

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 35s, `build/patchouli-backend-20260907-183831.log`.
- `python3 scripts/verify_artifacts.py` and `python3 scripts/verify_quartz_resources.py`: every target passed.
- Existing JUnit XML reports: 11 tests per leaf, zero failures/errors/skips. These remain utility tests, not book-interaction tests.
- `python3 scripts/test_audit_guidebook.py`: 2 tests passed. Inventory generation and `--check` passed against the pinned source.
- Alternate-state verification: set active project to 1.20.1 Forge, build all leaves, run both artifact checkers, reset to canonical 1.21.1 Fabric. Every Gradle command used a ten-minute deadline and `--no-daemon`. Exit, restore, source diff and controller diff all 0, 21s, `build/patchouli-roundtrip-20260907-184732.log`; byte snapshot `/tmp/arcane-patchouli-roundtrip-W0UpMT`.
- Servers: `python3 scripts/smoke_servers.py --quartz`. The existing isolated-world fixture additionally inserts and verifies `patchouli:guide_book`, then cleans up and saves/stops. This proves item registration, not an Arcane Archives book binding.
- Clients: per leaf, `xvfb-run -a --server-args='-screen 0 1280x800x24 -nolisten tcp' python3 scripts/smoke_clients.py <leaf>`. The checker now requires Patchouli's `BookContentResourceListenerLoader` preload message as well as the existing startup/model checks. Zero preloaded book JSONs is expected while our tome is absent; it is not page parity.
- Both smoke scripts wrap each underlying Gradle client/server task in `timeout --foreground 10m`, use `--no-daemon --console=plain`, save complete logs and stop their development processes. Screenshots remain outside the repository.

| Leaf | Side | Exit | Duration | Log |
|---|---|---|---|---|
| 1.20.1-fabric | server | 0 | 22.8s | `build/smoke-server-1.20.1-fabric-20260907-184156-950329.log` |
| 1.20.1-forge | server | 0 | 26.3s | `build/smoke-server-1.20.1-forge-20260907-184219-749627.log` |
| 1.21.1-fabric | server | 0 | 19.04s | `build/smoke-server-1.21.1-fabric-20260907-184246-049300.log` |
| 1.21.1-neoforge | server | 0 | 18.54s | `build/smoke-server-1.21.1-neoforge-20260907-184305-090436.log` |
| 1.20.1-fabric | client | 0 | 22.69s | `build/smoke-client-1.20.1-fabric-1788806603718352798.log` |
| 1.20.1-forge | client | 0 | 24.7s | `build/smoke-client-1.20.1-forge-1788806626499162883.log` |
| 1.21.1-fabric | client | 0 | 22.71s | `build/smoke-client-1.21.1-fabric-1788806651280204697.log` |
| 1.21.1-neoforge | client | 0 | 22.7s | `build/smoke-client-1.21.1-neoforge-1788806674075426170.log` |

Observed warnings are not hidden by these pass results: NeoForge emits Patchouli's missing-development-refmap warning; Fabric 1.21.1 reports removing an incompatible saved pack selection (the next resource-manager reload still explicitly includes `patchouli`); Forge initializes/corrects its new Patchouli client config. The dependency packs declare legacy resource formats (8 for 1.20.1; 32 for 1.21.1). No third-party artifact or user options were rewritten to suppress warnings. Interactive book rendering, resource reload while a book is open, optional-mod conditions, missing-dependency rejection and packaged-distribution runtime verification remain pending.
