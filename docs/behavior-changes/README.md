# Gameplay and logic change register

No gameplay or logic deviations are approved or implemented by the bootstrap. Missing content is unfinished migration, not a decision to remove it. Mechanical loader/API translation must preserve observable behavior.

Before any deviation, create one numbered Markdown file per change containing: status (proposed/approved/rejected/implemented), exact upstream source and behavior, proposed behavior, reason, affected targets, save/network compatibility impact, alternatives, user approval evidence and regression tests. Keep unrelated changes in separate files. Security fixes still require a documented decision; never reproduce unsafe behavior merely for parity.

Pending decision records:
- [Guidebook backend](0001-guidebook-backend.md)
- [Wearable integration](0002-wearable-integration.md)
