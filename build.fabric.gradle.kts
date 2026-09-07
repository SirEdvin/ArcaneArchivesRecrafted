plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-fabric"
val requiredJava = if (sc.current.parsed >= "1.20.5") 21 else 17

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava)
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
        "java" to requiredJava,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}
