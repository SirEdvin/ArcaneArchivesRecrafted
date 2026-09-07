# Arcane Archives Recrafted

## Scope

- Port https://github.com/AranaiRa/ArcaneArchives to Minecraft 1.20.1 (Fabric and Forge) and 1.21.1 (Fabric and NeoForge). Forge for 1.20.1 is explicitly approved.
- Use a Stonecutter multi-version layout and the minecraft-modding skill's `references/stonecutter-build-contracts.md` guidance.
- Preserve upstream gameplay and logic by default. Inspect upstream source and licensing before copying code or assets.
- Track migration work in `docs/MIGRATION_TASKS.md` with evidence-backed completion checkboxes.
- Record every proposed gameplay or logic change in a separate Markdown file under `docs/behavior-changes/`, including original behavior, proposed behavior, reason, affected targets, approval status, and verification. Obtain approval before implementing deviations.
- Ask the user first when requirements, tooling, compatibility, or licensing present an issue; do not silently work around it.

## Build and verification

Log handling is important. Always use an explicit timeout and silently save complete output.

Copied from UnlimitedPeripheralWorks `AGENTS.md`:

- Build: `mkdir -p build; LOG="build/gradle-$(date +%Y%m%d-%H%M%S).log"; timeout --foreground 10m ./gradlew build --no-daemon >"$LOG" 2>&1`

Increase timeouts only when required. Report the command, exit code, duration, log path, and relevant errors; inspect only the relevant failure window. Apply this logging pattern to targeted Gradle tasks too. Verify every supported version/loader leaf; a successful active-leaf build alone is not matrix verification. Stop development clients and servers after collecting results.

## Conventions

- Follow YAGNI principles, and prefer one-liner solutions when readable and correct.
- Keep shared behavior shared; isolate loader APIs and version-specific differences using the verified Stonecutter conventions.
- Preserve dedicated-server safety, server-authoritative networking, validation, and data-loss prevention.
- Never edit generated build output or commit build directories, run directories, logs, credentials, or EULA files.
- Do not claim migration parity from compilation alone; verify persistence, multiplayer, rendering, resources, and integrations as relevant.
- Do not commit, push, or create remotes without explicit authorization.
