import com.github.breadmoirai.githubreleaseplugin.GithubReleaseExtension
import com.modrinth.minotaur.ModrinthExtension
import net.darkhax.curseforgegradle.TaskPublishCurseForge

buildscript {
    repositories { mavenCentral(); gradlePluginPortal() }
    dependencies {
        classpath("com.github.breadmoirai:github-release:2.5.2")
        classpath("net.darkhax.curseforgegradle:CurseForgeGradle:1.1.24")
        classpath("com.modrinth.minotaur:Minotaur:2.9.0")
    }
}
apply<com.github.breadmoirai.githubreleaseplugin.GithubReleasePlugin>()

// Adapted from SirEdvin/modding-buildenv (MIT); see docs/upstream/modding-buildenv-LICENSE.
// Use its publishing plugins without importing its conflicting loader-plugin dependencies.
val releaseLeaves = listOf("1.20.1-fabric", "1.20.1-forge", "1.21.1-fabric", "1.21.1-neoforge")
val releaseVersion = providers.fileContents(layout.projectDirectory.file("stonecutter.properties.toml")).asText.map {
    Regex("""(?m)^mod.version = "([^"]+)"$""").find(it)!!.groupValues[1]
}.get()
val releaseNotes = layout.projectDirectory.file("docs/releases/$releaseVersion.md")
val releaseBody = providers.fileContents(releaseNotes).asText
val releaseType = if ('-' in releaseVersion) "beta" else "release"
val publishingDryRun = providers.gradleProperty("publishingDryRun").map(String::toBoolean).orElse(false)
val verifyReleaseArtifacts by tasks.registering(Exec::class) {
    group = "verification"
    description = "Build/test all supported leaves and verify production/source artifacts."
    dependsOn(releaseLeaves.map { ":$it:build" })
    workingDir(rootDir)
    commandLine("python3", "scripts/verify_artifacts.py")
}

val prepareGithubRelease by tasks.registering(Exec::class) {
    group = "publishing"
    description = "Validate immutable release state and generate checksums; never uploads."
    dependsOn(verifyReleaseArtifacts)
    workingDir(rootDir)
    commandLine("python3", "scripts/prepare_github_release.py")
    if (publishingDryRun.get()) args("--offline")
}

configure<GithubReleaseExtension> {
    owner.set("SirEdvin")
    repo.set("ArcaneArchivesRecrafted")
    tagName.set(releaseVersion)
    releaseName.set("Arcane Archives Recrafted $releaseVersion")
    targetCommitish.set(releaseVersion)
    setToken(providers.environmentVariable("GITHUB_TOKEN"))
    body.set(releaseBody)
    draft.set(false)
    prerelease.set(releaseType != "release")
    overwrite.set(false)
    allowUploadToExisting.set(false)
    dryRun.set(publishingDryRun)
    releaseAssets.from(releaseLeaves.flatMap { leaf ->
        val (minecraft, loader) = leaf.split('-', limit = 2)
        listOf("", "-sources").map { suffix ->
            layout.projectDirectory.file("versions/$leaf/build/libs/arcanearchives-$loader-$releaseVersion+$minecraft$suffix.jar")
        }
    }, layout.buildDirectory.file("SHA256SUMS"))
}
tasks.named("githubRelease") {
    dependsOn(prepareGithubRelease)
    notCompatibleWithConfigurationCache("Third-party publishing plugin uses Project at execution time")
}
tasks.register("publishGithub") {
    group = "publishing"
    description = "Compatibility alias for the GitHub Release plugin."
    dependsOn("githubRelease")
}

subprojects {
    if (name !in releaseLeaves) return@subprojects
    apply<net.darkhax.curseforgegradle.CurseForgeGradlePlugin>()
    apply<com.modrinth.minotaur.Minotaur>()
    val (minecraft, loader) = name.split('-', limit = 2)
    val loaderLabel = mapOf("fabric" to "Fabric", "forge" to "Forge", "neoforge" to "NeoForge").getValue(loader)
    val curseforgeId = providers.gradleProperty("curseforgeProjectId").orElse("")
    val modrinthId = providers.gradleProperty("modrinthProjectId").orElse("")
    val curseforgeToken = providers.environmentVariable("CURSEFORGE_TOKEN").orElse("")
    val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN").orElse("")
    val requiredMods = if (loader == "fabric") listOf("patchouli", "fabric-api") else listOf("patchouli")
    afterEvaluate {
        val output = tasks.named(if (loader == "fabric") "remapJar" else "jar", AbstractArchiveTask::class.java)
        configure<ModrinthExtension> {
            token.set(if (publishingDryRun.get()) "dry-run" else modrinthToken.get())
            projectId.set(modrinthId)
            versionNumber.set("$releaseVersion+$minecraft-$loader")
            versionName.set("$releaseVersion — $minecraft $loaderLabel")
            versionType.set(releaseType)
            uploadFile.set(output)
            gameVersions.set(listOf(minecraft))
            loaders.set(listOf(loader))
            detectLoaders.set(false)
            changelog.set(releaseBody)
            debugMode.set(publishingDryRun)
            failSilently.set(false)
            dependencies { requiredMods.forEach(required::project) }
        }
        tasks.named("modrinth") {
            dependsOn(rootProject.tasks.named("verifyReleaseArtifacts"))
            notCompatibleWithConfigurationCache("Third-party publishing plugin uses Project at execution time")
            doFirst {
                require(modrinthId.get().isNotBlank()) { "Set -PmodrinthProjectId to this mod's project ID" }
                require(publishingDryRun.get() || modrinthToken.get().isNotBlank()) { "Set MODRINTH_TOKEN" }
            }
        }
        tasks.register<TaskPublishCurseForge>("publishCurseForge") {
            group = "publishing"
            description = "Upload the $minecraft $loaderLabel production JAR to CurseForge."
            dependsOn(rootProject.tasks.named("verifyReleaseArtifacts"))
            notCompatibleWithConfigurationCache("Third-party publishing plugin uses Project at execution time")
            apiToken = if (publishingDryRun.get()) "dry-run" else curseforgeToken.get()
            debugMode = publishingDryRun.get()
            disableVersionDetection()
            if (curseforgeId.get().isNotBlank()) {
                val mainFile = upload(curseforgeId.get(), output.get().archiveFile)
                mainFile.displayName = "$releaseVersion — $minecraft $loaderLabel"
                mainFile.releaseType = releaseType
                mainFile.addGameVersion(minecraft)
                mainFile.addModLoader(loaderLabel)
                mainFile.addEnvironment("Client", "Server")
                mainFile.changelog = releaseBody
                mainFile.changelogType = "markdown"
                requiredMods.forEach { mainFile.addRequirement(it) }
            }
            doFirst {
                require(curseforgeId.get().toLongOrNull()?.let { it > 0 } == true) { "Set -PcurseforgeProjectId to this mod's numeric project ID" }
                require(publishingDryRun.get() || curseforgeToken.get().isNotBlank()) { "Set CURSEFORGE_TOKEN" }
            }
        }
    }
}
tasks.register("publishCurseForge") {
    group = "publishing"
    description = "Publish all four production JARs to CurseForge."
    dependsOn(releaseLeaves.map { ":$it:publishCurseForge" })
}
tasks.register("publishModrinth") {
    group = "publishing"
    description = "Publish all four production JARs to Modrinth."
    dependsOn(releaseLeaves.map { ":$it:modrinth" })
}
