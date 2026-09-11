#!/usr/bin/env python3
"""Verify the documented KubeJS example reaches the native Gem Cutter catalog.

Uses isolated development servers and existing EULA/loopback preflights.
Never overwrites an existing fixture script; removes only the script it installs.
Checks initial load and /reload; does not verify crafting or connected viewers.
"""
import argparse
import json
import os
from pathlib import Path


from smoke_servers import ROOT, preflight, smoke

NODES = ("1.20.1-fabric", "1.20.1-forge", "1.21.1-neoforge")
PROBE = """
// Test-only removal, never part of the distributed example or default recipes.
ServerEvents.recipes(event => {
  var matches = 0
  event.forEachRecipe({ id: 'arcanearchives:shaped_quartz' }, recipe => { matches++ })
  if (matches !== 1) throw new Error('Removal fixture must start with one shipped recipe')
  event.remove({ id: 'arcanearchives:shaped_quartz' })
  console.info('AA_KUBEJS_REMOVAL_REQUESTED')
})
var aaRecipeChecked = false
function aaCheckRecipe(server) {
  var Recipes = Java.loadClass('com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe')
  var entries = Recipes.entries(server.getRecipeManager())
  let found = 0
  entries.forEach(entry => {
    if (String(entry.name()) === 'arcanearchives:shaped_quartz')
      throw new Error('Removed recipe remains in native catalog')
    if (String(entry.name()) !== 'kubejs:arcane_example') return
    found++
    var recipe = entry.recipe().definition(entry.name())
    if (recipe.getRecipeOutput().getCount() !== 4) throw new Error('Output count lost')
    var costs = new (Java.loadClass('java.util.ArrayList'))(recipe.getIngredients())
    if (costs.size() !== 2 || costs.get(0).getCount() !== 2 || costs.get(1).getCount() !== 3)
      throw new Error('Counted costs lost')
  })
  if (found !== 1) throw new Error('Expected exactly one native example recipe, got ' + found)
  console.info('AA_KUBEJS_NATIVE_ASSERTIONS_PASSED')
}
ServerEvents.loaded(event => {
  aaRecipeChecked = true
  try {
    aaCheckRecipe(event.server)
  } catch (failure) {
    event.server.runCommandSilent('say AA_KUBEJS_NATIVE_RECIPE')
    throw failure
  }
})
// Server scripts are reevaluated by /reload. loaded is not fired again;
// the first subsequent server tick verifies the newly installed native catalog.
ServerEvents.tick(event => {
  if (aaRecipeChecked) return
  aaRecipeChecked = true
  try {
    aaCheckRecipe(event.server)
  } finally {
    event.server.runCommandSilent('say AA_KUBEJS_NATIVE_RECIPE')
  }
})
"""


DELETE_PROBE = """
var aaDeletionChecked = false
ServerEvents.tick(event => {
  if (aaDeletionChecked) return
  aaDeletionChecked = true
  try {
    var Recipes = Java.loadClass('com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe')
    var restored = 0
    Recipes.entries(event.server.getRecipeManager()).forEach(entry => {
      if (String(entry.name()) === 'kubejs:arcane_example')
        throw new Error('Deleted script recipe remains in native catalog')
      if (String(entry.name()) === 'arcanearchives:shaped_quartz') {
        restored++
        var recipe = entry.recipe().definition(entry.name())
        if (recipe.getRecipeOutput().getCount() !== 1)
          throw new Error('Restored shipped recipe output changed')
        var costs = new (Java.loadClass('java.util.ArrayList'))(recipe.getIngredients())
        if (costs.size() !== 1 || costs.get(0).getCount() !== 2)
          throw new Error('Restored shipped recipe costs changed')
      }
    })
    if (restored !== 1) throw new Error('Expected one restored shipped recipe, got ' + restored)
    console.info('AA_KUBEJS_DELETION_ASSERTIONS_PASSED')
  } finally {
    event.server.runCommandSilent('say AA_KUBEJS_NATIVE_RECIPE')
  }
})
"""


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--edit", action="store_true", help="Change output count in the live script before /reload")
    mode.add_argument("--delete", action="store_true", help="Delete the live recipe script before /reload")
    args = parser.parse_args()
    os.environ["ORG_GRADLE_PROJECT_optionalIntegrationMods"] = "jade,jei,emi,kubejs"
    paths = [ROOT / "versions" / node / "runs/server/kubejs/server_scripts/aa_recipe_probe.js" for node in NODES]
    for node, path in zip(NODES, paths):
        preflight(node)
        if path.exists():
            raise SystemExit(f"Refusing to overwrite {path}")
        observer = path.with_name("aa_recipe_deletion_observer.js")
        if args.delete and observer.exists():
            raise SystemExit(f"Refusing to overwrite {observer}")
    source = (ROOT / "docs/examples/kubejs/gem_cutting.js").read_text() + PROBE
    results = []
    for node, path in zip(NODES, paths):
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("x") as output:
            output.write(source)
        observer = path.with_name("aa_recipe_deletion_observer.js")
        observer_created = False
        try:
            previous = set((ROOT / "build").glob(f"smoke-server-{node}-*.log"))
            def edit_fixture():
                nonlocal observer_created
                if path.read_text() != source:
                    raise RuntimeError(f"Fixture changed externally: {path}")
                if args.delete:
                    with observer.open("x") as output:
                        observer_created = True
                        output.write(DELETE_PROBE)
                    path.unlink()
                    return
                edited = source.replace("count: 4", "count: 7").replace("getCount() !== 4", "getCount() !== 7")
                if edited == source or "count: 7" not in edited:
                    raise RuntimeError("Example output count not found")
                path.write_text(edited)

            passed = smoke(node, commands=["reload"], markers=("AA_KUBEJS_NATIVE_RECIPE",),
                           before_commands=edit_fixture if args.edit or args.delete else None)
            logs = set((ROOT / "build").glob(f"smoke-server-{node}-*.log")) - previous
            assert len(logs) == 1
            text = logs.pop().read_text()
            expected = 1 if args.delete else 2
            passed = passed and text.count('AA_KUBEJS_NATIVE_ASSERTIONS_PASSED') == expected
            passed = passed and text.count('AA_KUBEJS_REMOVAL_REQUESTED') == expected
            if args.delete:
                passed = passed and text.count('AA_KUBEJS_DELETION_ASSERTIONS_PASSED') == 1
            results.append({"node": node, "edited": args.edit, "deleted": args.delete, "passed": passed})
        finally:
            path.unlink(missing_ok=True)
            if observer_created:
                observer.unlink()
    print(json.dumps(results))
    return 0 if all(result["passed"] for result in results) else 1


if __name__ == "__main__":
    raise SystemExit(main())
