# Tank fluid surface adaptation

Status: world renderer implemented under delegated authority; all four targets compile/assemble (`build/storage-progress-20260908-183012.log`, exit 0, 17s). Visual acceptance remains pending.

Pinned upstream release `bb99accf48ed583e29b0efae56e28c963407b8df`, `client/render/RadiantTankTESR.java`: draws the native still/flowing fluid sprites and tint in a six-face volume, translates by (0.08,0.05,0.08), and uses coordinates 0.08..0.76, lower Y 0.05 and upper Y `(amount/capacity)*0.9`. Very small nonzero amounts therefore put the upper face below the lower face.

Port: preserve those coordinates, translation, cropped native sprite UV intent, tint and fill ratio, using the current block-entity renderer and buffered translucent pipeline rather than legacy global OpenGL state. Clamp the nonzero upper surface just above the lower surface so a tiny amount cannot form an inverted volume. Empty tanks render no fluid. Use each loader's native fluid sprite/tint provider; no substitute artwork is created.

This is a rendering-only adaptation with no capacity, amount, recipes, persistence or network format changes. Alternative rejected: reproducing inverted small-volume geometry or substituting a generic blue texture for modded fluids. Item fluid rendering and client visual validation remain unfinished.
