# 0002 — Wearable integration

Status: investigation required; no replacement approved or implemented.

Baseline: upstream `build.gradle` includes Baubles 1.12-1.5.2; `integration/baubles/BaubleBodyCapabilityHandler.java` and `BaubleGemUtil.java` implement equipment integration. Trace gem/charm callers before defining exact slot and activation behavior.

Proposed behavior: undecided. Preserve equip/unequip effects and avoid duplicate activation. Do not silently substitute Curios or Trinkets, require a new wearable mod, or disable wearable effects on Fabric.

Targets: all four leaves.

Compatibility: slot availability, item identity, charge persistence, death handling and optional-mod requirements need explicit decisions.

Alternatives: version-matched wearable APIs, an internal equipment surface, or a separately approved scope reduction. Availability and equivalence are not yet established.

Approval: none.

Verification: baseline slot/activation fixtures; equip/unequip, dimension travel, death and reconnect; optional-mod absence; no duplicate effects or lost charges.
