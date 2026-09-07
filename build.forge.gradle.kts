plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.143"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-forge"

legacyForge {
    version = "${sc.current.version}-${property("deps.forge_loader")}"
    mods.register(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(17)
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
    filesMatching("META-INF/mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}

tasks.named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }
