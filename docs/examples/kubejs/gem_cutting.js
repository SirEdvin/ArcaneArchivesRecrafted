// Place in kubejs/server_scripts/gem_cutting.js on supported KubeJS targets.
// Example only: not shipped as a default gameplay recipe. Apply with /reload.
ServerEvents.recipes(event => {
  // Use exact IDs to remove or replace recipes; do not rely on output-only filters.
  event.remove({ id: 'kubejs:arcane_example' })
  event.custom({
    type: 'arcanearchives:gem_cutting',
    inputs: [
      { item: 'minecraft:diamond', count: 2 },
      { tag: 'minecraft:planks', count: 3 }
    ],
    result: { item: 'minecraft:paper', count: 4 },
    order: 1000
  }).id('kubejs:arcane_example')
})
