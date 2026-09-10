// Explicit four-leaf release gate; gh is only the authenticated GitHub transport.
val releaseLeaves = listOf("1.20.1-fabric", "1.20.1-forge", "1.21.1-fabric", "1.21.1-neoforge")
val verifyReleaseArtifacts by tasks.registering(Exec::class) {
    group = "verification"
    description = "Build/test all supported leaves and verify production/source artifacts."
    dependsOn(releaseLeaves.map { ":$it:build" })
    workingDir(rootDir)
    commandLine("python3", "scripts/verify_artifacts.py")
}

tasks.register<Exec>("publishGithub") {
    group = "publishing"
    description = "Publish a new GitHub prerelease from a clean, committed, pushed tag."
    dependsOn(verifyReleaseArtifacts)
    workingDir(rootDir)
    commandLine("python3", "scripts/publish_github.py")
}
