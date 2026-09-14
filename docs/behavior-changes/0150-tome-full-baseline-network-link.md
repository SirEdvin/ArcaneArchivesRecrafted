# 0150 — Complete Tome baseline and remaining Networks link

Approval status: approved by the user ("yeyess"). Implemented and verified in complete-source preparation; production Patchouli page conversion remains pending.

Implementation evidence: `scripts/prepare_tome.py` now uses the audited development pin, adds only the approved Networks mapping, and rejects drift in both complete content counts and the 317-reference inventory before writing. Real pinned-source preparation validates all 317 targets and makes ten approved reference-occurrence repairs. Five preparation tests and six audit tests pass (`build/tome-full-preparation-20260913-175718.log`, exit 0, 1 s). Full parsed-tree comparison after undoing exactly those ten references preserves all 98 sections, comments, prose, attributes and conditions (`build/tome-full-preservation.log`, exit 0). No gameplay resources or Java changed; no Gradle/client campaign run. The generated XML remains intermediate material, not a playable book.

## Original behavior and source correction

The implementation plan's audited book is `src/main/resources/assets/arcanearchives/xml/tome.xml` at development pin `80944ce45c6559243d8928cc4b305bf379388652`. Direct `git show` and XML parsing confirm 7 chapters, 98 sections, 44 recipes, 256 links and 71 images.

The later `prepare_tome.py` incorrectly selected release pin `bb99accf48ed583e29b0efae56e28c963407b8df` for that full-content task. Its book contains 7 chapters, 78 sections, 42 recipes, 234 links and 68 images. Earlier preparation tests accurately validated that smaller document, but did not establish coverage of the plan's full book. Do not use its output to claim complete Tome conversion.

Development-only named sections include implemented content such as `Items:RadiantKey`, `Items:EmpoweredQuartz` and `Items:Echoes`, as well as dormant/excluded features. Restoring the planned content source does not authorize enabling dormant blocks, reintroducing the removed Radiant Furnace or inventing recipes. Their existing migration decisions remain authoritative.

## Additional broken navigation

Development `tome.xml:215` contains the literal reference `Concepts:Immanence` with visible label `Networks`. There is no `Immanence` section. The existing `Concepts:Networks` section starts at line 2193 with title `Networks` and condition `true`; its body documents automatic personal networks, invitations, resignation, Manifest listings and expulsion.

The original Guidebook parser treats the prefix/suffix as exact chapter/section names. Approval 0148 contains seven explicit mappings, but not this one. Running the current strict preparation function against the full planned source correctly fails with `Unresolved Tome target: 'Concepts:Immanence' -> 'Concepts:Immanence'` rather than guessing a repair.

## Proposed behavior

Add exactly `Concepts:Immanence` -> `Concepts:Networks` to the approved conversion mappings, preserving the visible label, surrounding text and visibility conditions. Continue rejecting other unresolved references rather than adding a heuristic.

Use the plan's complete development-pinned book for conversion and update pinned-count checks accordingly. Preserve the original source audit unchanged. Release-pinned gameplay/runtime and asset decisions elsewhere are not globally switched by this book-source correction.

## Reason and affected targets

Prevent omission of documented, implemented content and restore the existing Networks destination. All four targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. No change to actual network permissions, discovery, crafting, grants or storage.

## Verification

Completed: both pinned documents parsed directly from Git; exact counts compared; development-only named sections enumerated; missing old destination and unique existing Networks section inspected; current preparer rejection reproduced. No production book pages generated or navigation repair implemented.

After approval: switch preparation to the full audited pin, add this explicit mapping, verify all references and complete parsed-tree preservation, and carry full section coverage into production conversion checks. Native builds do not establish page/content completeness.
