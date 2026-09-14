# 0148 — Tome navigation target repairs

Approval status: approved by the user ("yes"). Implemented in the complete XML preparation step; production Patchouli conversion and runtime verification remain pending.

## Original behavior and evidence

The release-pinned `src/main/resources/assets/arcanearchives/xml/tome.xml` at `bb99accf48ed583e29b0efae56e28c963407b8df` contains references that do not resolve to its declared chapter/section structure.

The embedded Guidebook source was inspected: `ElementLink.parse` and `BookDocument` stack-link parsing both call `SectionRef.fromString`. A colon separates chapter and section; a bare reference names a chapter, not a globally searched section. `SectionRef.resolve` catches missing-target lookup failures and returns false. This is source evidence, not an upstream graphical reproduction.

A Python ElementTree traversal checked all 280 explicit internal link references and top-level stack-link targets in the pinned checkout. Nine occurrences have missing targets (six ordinary links and three stack links). Each proposed destination below exists and is the unique section with that exact section ID. Original tokens are retained here rather than silently normalized.

## Proposed behavior

Apply only these explicit mappings during Patchouli conversion:

| Original literal | Proposed existing target | Occurrences |
| --- | --- | --- |
| `RadiantResonator` | `Blocks:RadiantResonator` | 2 ordinary links |
| `Items:RadiantCraftingTable` | `Blocks:RadiantCraftingTable` | 1 ordinary link |
| `Blocks:SliverOfLight` | `Items:SliverOfLight` | 1 ordinary link + 1 stack link |
| `MonitoringCrystal` | `Blocks:MonitoringCrystal` | 1 ordinary link |
| `Parchtear` | `Gems:Parchtear` | 1 ordinary link |
| `Blocks:RawQuartz` | `Items:RawQuartz` | 1 stack link |
| `Blocks:ShapedQuartz` | `Items:ShapedQuartz` | 1 stack link |

Preserve surrounding prose, link labels, visibility conditions and item identities. In particular, repairing the Parchtear destination does not enable Arsenal or bypass its visibility gates. Do not add a general heuristic that repairs arbitrary unresolved source tokens. Keep the upstream audit unchanged and fail future conversion validation on any unresolved reference not covered by an approved explicit mapping.

## Reason and affected targets

A complete usable Tome should navigate to the existing intended content rather than carry broken source navigation into Patchouli. This changes book navigation, not crafting, inventories, acquisition, progression or permissions.

Targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification

`scripts/prepare_tome.py` reads the exact release pin through `git show`, preserves a detached complete XML tree and applies only the approved literal map. Unresolved targets, missing approved destinations, duplicate named chapters/sections and pinned reference-count drift fail before writing. It does not register a partial book or edit the upstream audit. The output is an intermediate XML document, not production Patchouli JSON.

`build/tome-preparation-20260913-164358.log`: exit 0, under one second (shell timer 0 s); five preparation tests and six existing audit tests passed. The real pinned source produced `build/tome-prepared.xml`, with all 280 targets validated and nine repairs. An independent full parsed-document round-trip undid only those nine occurrences and matched the source tree exactly, including comments, text, conditions and structure (`build/tome-preparation-preservation.log`, exit 0). XML serialization itself is not byte-identical to the source. No Java/resources changed, so no Gradle/client campaign was run for this step.

Completed: original parser/resolver inspection, full explicit-reference traversal and unique proposed target validation. No runtime code, book resources or source tokens were changed. `git diff --check` is required for this proposal.

After approval: regression-test the explicit mappings and unknown-target rejection during conversion; validate every generated internal destination and retained visibility condition; run all four build/artifact checks. Actual book clicking and presentation remain part of the user's initial test, not established by static validation.
