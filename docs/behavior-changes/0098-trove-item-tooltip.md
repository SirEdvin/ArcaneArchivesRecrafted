# 0098 — Packed Trove tooltip

Status: implemented and assembly/package-verified on all four targets; live presentation acceptance remains open.

Original: release bb99accf48ed583e29b0efae56e28c963407b8df, items/itemblocks/RadiantTroveItem.java:37–58. Packed Troves show reference-item name with rarity color and italic custom names, actual count/capacity, and a dark-purple bold voiding warning. Unpacked items retain only the generic block tooltip. Both original language files use English for these lines.

Restore this presentation through a dedicated BlockItem and the existing validated Trove block-entity decoder. Use a copied tag and detached block entity; no world lookup, tag creation or item mutation. An empty locked Trove shows its retained filter but count zero. Preserve the port's approved capacity/conservation semantics rather than duplicating legacy capacity math. Invalid packed data gets a red diagnostic rather than silently rewriting it. Modern translation placeholders use %s (the native component formatter does not support legacy %d).

Approval: source-backed restoration and existing defensive-read/conservation policy; no new gameplay deviation. Item inventory capabilities, networks and rendering are separate work, not claimed complete here.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*RadiantTroveItemTest' --offline --no-daemon`, exit 0, 27s, `build/trove-item-tooltip-20260909-213657.log`. Four targets assembled; NeoForge XML confirms 3 tests, no skips/failures/errors. Fixtures exercise named/rare extended contents, Matrix Brace capacity, VOID styling, empty LOCK reference with zero quantity, unpacked/packed-empty/invalid data and read-only item state.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/trove-item-tooltip-artifacts.log`: four production/source pairs passed. No client/server launched. Connected hover rendering, modded rarity, multiplayer and actual save/restart acceptance remain open.
