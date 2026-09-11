# Publishing

## Implementation and provenance

`release.gradle.kts` adapts the publishing setup used by UnlimitedPeripheralWorks (`c627a36f3abcbc16a973641ad694a6886496f0ef`) and modding-buildenv (`63221f143e50f80cac9cd8b54fe846c4f844409f`, `site.siredvin.release` / `site.siredvin.mod-publishing`). The copied configuration is MIT-licensed; its notice is retained in `docs/upstream/modding-buildenv-LICENSE`.

Direct application of buildenv 0.9.1 resolves on Gradle 9.6.1, but brings its older loader plugins onto the classpath. An isolated dependency-cache probe combining it with Loom 1.17.12 fails because Loom is already on the classpath with an unknown version (`build/buildenv-loader-compat-probe.log`). Its default project layout and release tags also assume one Minecraft version and `:fabric`/`:forge` leaves. We therefore reuse the underlying publishing plugins, without replacing the working loader build:

- GitHub: `com.github.breadmoirai.github-release` 2.5.2.
- CurseForge: `net.darkhax.curseforgegradle` 1.1.24.
- Modrinth: `com.modrinth.minotaur` 2.9.0 (the version resolved from buildenv's `2.+` dependency).

There is no custom upload transport. `scripts/prepare_github_release.py` only validates the clean tree, exact local/remote tag and absent GitHub release, and generates checksums. It never uploads. `gh` is needed for that read-only preflight; the GitHub Release plugin performs publication.

## Preparation and verification

Set `mod.version` in `stonecutter.properties.toml`, and add `docs/releases/<version>.md`. A version without `-` is a normal release; prerelease versions are GitHub prereleases and beta files on CurseForge/Modrinth. Minecraft 1.20.1 uses Java 17, 1.21.1 uses Java 21.

All publishing tasks depend on `verifyReleaseArtifacts`, which builds/tests all four targets and checks exact production/source JAR contracts. Fabric uploads use `remapJar`; Forge and NeoForge use their production `jar` task. No unremapped development JAR is published.

Use the mandatory bounded/full-log pattern for every Gradle command, for example:

```bash
mkdir -p build
LOG="build/release-$(date +%Y%m%d-%H%M%S).log"
timeout --foreground 10m ./gradlew verifyReleaseArtifacts --no-daemon >"$LOG" 2>&1
```

Publishing plugins use Gradle Project APIs at execution time: pass `--no-configuration-cache` for publication. Normal build/test configuration caching remains enabled.

## GitHub

1. Commit/push the release changes, create an unused exact version tag (e.g. `0.0.1`, no leading `v`) pointing at HEAD, and push it.
2. Supply `GITHUB_TOKEN` securely in the environment. If using an authenticated local `gh`, scope `GITHUB_TOKEN="$(gh auth token)"` to the Gradle command; do not print/store it or enable shell tracing.
3. Run `./gradlew githubRelease --no-configuration-cache --no-daemon` with full logging and timeout. `publishGithub` is an alias.
4. Read back release status, exact asset names/sizes, and download/verify hashes before reporting success.

The task publishes four production JARs, four source JARs and SHA256SUMS. Overwriting or appending to an existing release is disabled. The task requires a clean tree and a pushed tag identifying HEAD.

`-PpublishingDryRun=true` enables the plugin's no-write mode and an offline artifact preflight. The GitHub plugin still performs authenticated read requests, so a valid `GITHUB_TOKEN` is required even in dry-run mode. Do not confuse a dry run with a published release.

## CurseForge and Modrinth

These integrations are configured but project IDs are intentionally not guessed. Supply IDs owned by this mod, not upstream Arcane Archives or UnlimitedPeripheralWorks:

| Service | Task for all targets | Project property | Environment secret |
| --- | --- | --- | --- |
| CurseForge | `publishCurseForge` | `-PcurseforgeProjectId=<numeric ID>` | `CURSEFORGE_TOKEN` |
| Modrinth | `publishModrinth` | `-PmodrinthProjectId=<ID or slug>` | `MODRINTH_TOKEN` |

Use the same timeout/full-log pattern, adding `--no-configuration-cache --no-daemon`. Individual target tasks are `:<minecraft>-<loader>:publishCurseForge` and `:<minecraft>-<loader>:modrinth`.

Each file declares exactly one Minecraft version and loader. Modrinth version identifiers include all three identities (`0.0.1+1.20.1-fabric`, etc.) to avoid collisions. Patchouli is required everywhere; Fabric API is additionally required on Fabric. Optional integrations are not falsely marked required. CurseForge explicitly declares Client and Server environments and the Forge/NeoForge distinction, rather than relying on plugin detection.

Missing project IDs/tokens fail explicitly; normal `build` does not require them. Multi-file publishing is not atomic: if a platform partially accepts files, inspect its remote state and rerun only the missing leaf tasks. Never blindly rerun an aggregate upload.

`-PpublishingDryRun=true` selects native debug/no-write modes. GitHub and all four Modrinth debug runs passed during implementation. CurseForge's debug run with a non-publishing placeholder configuration failed fetching game-version types (HTTP 400 from `https://minecraft.curseforge.com/api/game/version-types`); no CurseForge upload is verified. Read-only assertions verified its four artifact/loader/release configurations. Real CurseForge connectivity and platform-owned IDs/tokens are still prerequisites before publication; do not bypass this failure or claim remote success.
