# Four-target CI

## Implementation

`.github/workflows/build.yml` runs on pushes, pull requests and manual dispatch with read-only repository permissions and no publishing. All three actions are pinned to commit SHAs resolved from their upstream latest-release tags; their actual `action.yml` inputs were inspected. Checkout does not persist credentials.

The job installs Temurin 17 and 21, builds every Stonecutter leaf with `build --continue --no-daemon`, then verifies all production/source artifact pairs. There is deliberately no restored Gradle cache, so hosted runs will exercise fresh dependency resolution. A ten-minute build timeout and twenty-minute job timeout bound execution. Complete Gradle/verification output is redirected to files. Reports upload even after failure; mod jars upload only after successful verification. Run directories, credentials and EULA files are not uploaded. Artifact archiving is explicit.

## Local execution evidence

- Fresh `GRADLE_USER_HOME=/tmp/arcane-ci-gradle-zDZMa5BD`: `timeout --foreground 10m ./gradlew help --no-daemon --no-configuration-cache` passed, exit 0 in 87s. Full log: `build/ci-clean-resolution-20260911-143014.log` in the main repository.
- Created a disposable source-only copy at `/tmp/arcane-ci-source-1789137198532760698`, without existing generated builds or project caches. It contains the current accumulated migration sources, not only the last published commit.
- With that isolated Gradle home, `timeout --foreground 10m ./gradlew build --continue --no-daemon` passed, exit 0 in 384s. Full log: `/tmp/arcane-ci-source-1789137198532760698/build/ci-clean-build.log`. All four native GameTest suites passed (7/8 required tests according to loader), along with the build checks.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py` passed all four production/source pairs, exit 0 in 1s. Full log: `/tmp/arcane-ci-source-1789137198532760698/build/ci-artifacts.log`.
- Parsed workflow YAML, checked triggers/read-only permissions, and syntax-checked both shell blocks using `bash -n`. No actionlint executable is installed; this is not an actionlint result.

The isolated Gradle home started empty for configuration resolution, then was reused for the source-only build. Available local Java installations were still discoverable; this is not proof of hosted Temurin provisioning or a fully isolated operating system.

## Remaining gate

Hosted GitHub execution is unverified. No commit, push, workflow dispatch or release was performed. The repository contains accumulated uncommitted migration work, so publishing only the workflow would not test this exact source tree. Obtain explicit authorization for the intended commit/push scope before publishing and checking the hosted run. Keep the parent CI migration task open until that gate passes; no release publishing is configured.
