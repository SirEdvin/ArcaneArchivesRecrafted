# Gem Cutter recipe scripting

Supported KubeJS targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 NeoForge. KubeJS is optional and is explicitly excluded on 1.21.1 Fabric. Use the pinned versions in `stonecutter.properties.toml` and their Rhino/Architectury companions.

Copy `gem_cutting.js` into your instance's `kubejs/server_scripts/` directory, then run `/reload` with operator permission (or restart). This example deliberately adds a pack-authored recipe, not a default mod recipe. It consumes two diamonds and three planks to produce four paper. Remove the script and reload to stop adding it.

The integration uses KubeJS `ServerEvents.recipes`, `event.custom({...}).id(...)` and the mod's native `arcanearchives:gem_cutting` serializer. No second crafting implementation is involved. Use exact namespaced recipe IDs for removal:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'arcanearchives:shaped_quartz' })
})
```

That removal is an intentional pack change; it is not installed by the mod or the example. To replace a shipped recipe, remove its exact ID and add a custom recipe with that ID in the same recipe event. This does not reproduce legacy CraftTweaker's output-only matching contract, so do not assume output-filter or generic ingredient-replacement helpers understand this custom JSON format.

## Native JSON limits

Each input uses exactly one namespaced `item` or `tag` and a positive integer `count` (default one). Output uses `item` and positive `count`, bounded by the item's native stack limit and 64. Optional `order` controls catalog ordering. Arbitrary input/output NBT or components are deliberately rejected and excluded from scope by the user under [0118](../../behavior-changes/0118-scripted-stack-data.md), not pending implementation. Unknown fields are rejected except the validated KubeJS source metadata described in behavior change 0117.

## Executed verification

`timeout --foreground 32m python3 scripts/smoke_kubejs_recipes.py` enables the opt-in Jade+JEI+EMI+KubeJS development profile and requires pre-existing EULA acceptance and loopback server configuration. It installs only its own temporary script, refuses an existing file at that path, and removes its script after each run. Never run this fixture against a production world.

The fixture verifies the shipped shaped-quartz recipe exists before requesting removal, then checks the native catalog for its absence and exactly one counted example recipe. It repeats both event-time and native-catalog assertions after a real `/reload`, catching duplicate accumulation or stale removed entries. Server shutdown/save assertions must also pass.

All three leaves passed, aggregate exit 0, 94 seconds, `build/kubejs-remove-reload.log`:

- Fabric 1.20.1: 35.65s, `build/smoke-server-1.20.1-fabric-20260910-194547-919688.log`.
- Forge 1.20.1: 38.10s, `build/smoke-server-1.20.1-forge-20260910-194623-576905.log`.
- NeoForge 1.21.1: 20.04s, `build/smoke-server-1.21.1-neoforge-20260910-194701-683404.log`.

Each runtime log contains two native-success markers and two removal-request markers, with no ERROR lines. Temporary scripts were removed. This verifies addition/removal and repeat reload of unchanged scripts.

### Live script edits

`timeout --foreground 5m python3 scripts/smoke_kubejs_recipes.py --edit` passed all three supported leaves, exit 0, 91 seconds (`build/kubejs-live-edit.log`). After server readiness, the harness changes only its owned fixture's output count from four to seven before sending `/reload`. Initial native assertions require four; post-reload assertions require seven, unchanged counted inputs, exactly one matching recipe and continued removal of the shipped fixture recipe.

- Fabric 1.20.1: 35.12s, `build/smoke-server-1.20.1-fabric-20260910-195734-694690.log`.
- Forge 1.20.1: 36.59s, `build/smoke-server-1.20.1-forge-20260910-195809-816280.log`.
- NeoForge 1.21.1: 19.29s, `build/smoke-server-1.21.1-neoforge-20260910-195846-404916.log`.

All native/event assertion counts and shutdown/save checks passed, with no ERROR lines. No fixture scripts or project JVMs remained.

### Live script deletion

`timeout --foreground 5m python3 scripts/smoke_kubejs_recipes.py --delete` passed all three supported leaves, exit 0, 83 seconds (`build/kubejs-live-delete.log`). After initial native assertions and server readiness, the harness deletes its recipe script and installs a separate read-only assertion observer before `/reload`. The custom example must disappear; the previously removed shipped shaped-quartz recipe must return exactly once, retaining its output count of one and input cost of two.

- Fabric 1.20.1: 25.05s, `build/smoke-server-1.20.1-fabric-20260910-200055-172818.log`.
- Forge 1.20.1: 30.08s, `build/smoke-server-1.20.1-forge-20260910-200120-228513.log`.
- NeoForge 1.21.1: 28.08s, `build/smoke-server-1.21.1-neoforge-20260910-200150-307492.log`.

Initial and deletion assertions passed on every leaf, as did shutdown/save checks. No ERROR lines, project JVMs or fixture scripts remained. Connected recipe viewers and actual paid crafting remain separate acceptance work.
