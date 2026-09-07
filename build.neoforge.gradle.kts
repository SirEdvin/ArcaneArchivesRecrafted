plugins {
    id("net.neoforged.moddev") version "2.0.143"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-neoforge"

neoForge {
    version = property("deps.neoforge_loader") as String
    mods.register(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.processResources {
    val props = mapOf(
        "id" to project.property("mod.id"),
        "name" to project.property("mod.name"),
        "version" to project.version.toString(),
        "license" to project.property("mod.license"),
        "authors" to project.property("mod.authors"),
        "description" to project.property("mod.description"),
        "minecraft" to project.property("mod.mc_compat"),
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/mods.toml")
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}
