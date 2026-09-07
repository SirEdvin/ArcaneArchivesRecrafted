# 0001 — Guidebook backend

Status: investigation required; no replacement approved or implemented.

Baseline: upstream `ArcaneArchives.java` requires `gbook_snapshot`; `build.gradle` embeds modified Guidebook 1.12.2-2.9.1.s5. `integration/guidebook/` supplies recipe integration; `integration/patchouli/TestProcessor.java` also exists. Source presence alone does not establish equivalent content.

Proposed behavior: undecided. Preserve guide content, unlock conditions and recipe displays. Investigate target-compatible backends before asking the user to choose; do not silently select Patchouli or remove the book.

Targets: all four leaves.

Compatibility: inventory identity, book opening, progression gates and dependency requirements may change. Review existing tome items and saved data before design.

Alternatives: port the original backend, use an available modern guidebook library, or implement the required book behavior locally; feasibility is unverified.

Approval: none.

Verification: compare page/content inventory and conditions against the baseline, test recipe reload and client book interactions, verify dedicated-server startup without client class loading.
