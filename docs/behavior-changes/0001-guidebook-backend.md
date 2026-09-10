# 0001 — Guidebook backend

Status: approved; external dependency/backend foundation implemented and startup-tested on all four targets after reviewing the publisher's external-dependency licensing guidance. Tome content, item and interaction implementation remain pending; see `../migration/GUIDEBOOK_BACKEND.md`.

Baseline: upstream `ArcaneArchives.java` requires `gbook_snapshot`; `build.gradle` embeds modified Guidebook 1.12.2-2.9.1.s5. `integration/guidebook/` supplies recipe integration; `integration/patchouli/TestProcessor.java` also exists. Source presence alone does not establish equivalent content.

Proposed behavior: use separately installed Patchouli as the required guidebook backend on all four leaves, preserving the real Guidebook content, unlock conditions and recipe displays. Port `assets/arcanearchives/xml/tome.xml` and its templates; do not substitute the experimental Patchouli book, whose title is `Arcane Tome Thingy` and landing text is `BUZZ`. Book UI and backend dependency change; loss of pages or conditions is not approved by this proposal.

Targets: all four leaves.

Compatibility: the approved change replaces the backend/UI and requires external Patchouli; it does not authorize dropping tome identity, content or progression behavior. Trace the upstream tome item, opening/acquisition routes and current-world persistence before implementing them. Old-save import or conversion remains out of scope under the fresh-world-only decision.

Availability evidence: public Modrinth release metadata identifies Patchouli `1.20.1-85-fabric`, `1.20.1-85-forge`, `1.21.1-93-fabric`, and `1.21.1-93-neoforge`. Exact version IDs, dependency metadata and download hashes are captured in `../migration/integration-candidates.json`. Separately resolved and startup-tested Maven versions use uppercase loader suffixes, pinned in `../../stonecutter.properties.toml`; their actual hashes are in `../migration/patchouli-resolved-artifacts.json`. The 1.20.1 Maven and Modrinth binaries differ; the candidate hashes are not the tested Maven hashes.

Licensing boundary: the release sources confirm CC-BY-NC-SA-3.0 and recommend ordinary external dependencies/API consumption. That guidance was reviewed before wiring the dependency; source commits and findings are recorded in `../migration/GUIDEBOOK_BACKEND.md`. Do not copy/bundle its code or artwork into this MIT port. Release distribution obligations still require review; user approval is not permission to ignore licenses.

Alternatives: port the original Guidebook backend or implement the required behavior locally; modern feasibility is unverified and either entails maintaining more UI code.

Approval: the user explicitly replied “approve all three” to external Patchouli subject to licensing review, optional Curios/Trinkets, and leaving dormant features documented and disabled. This approves the backend choice, not content loss, bundling third-party code/artwork, or bypassing licensing review. Full book implementation and verification remain pending.

Verification: compare page/content inventory and conditions against the baseline, test recipe reload and client book interactions, verify dedicated-server startup without client class loading.

Completed evidence is limited to the backend foundation, source inventory and startup/item/resource checks in [GUIDEBOOK_BACKEND.md](../migration/GUIDEBOOK_BACKEND.md). Full book verification remains open. See [HANDOFF.md](../HANDOFF.md) for continuation order and the other approved decisions.
