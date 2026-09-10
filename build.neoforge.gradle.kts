plugins {
    id("net.neoforged.moddev") version "2.0.143"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-neoforge"

repositories {
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.blamejared.com/") { content { includeGroup("vazkii.patchouli") } }
}

dependencies {
    compileOnly("maven.modrinth:curios:${property("deps.curios")}")
    compileOnly("vazkii.patchouli:Patchouli:${property("deps.patchouli")}:api")
    runtimeOnly("vazkii.patchouli:Patchouli:${property("deps.patchouli")}")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

tasks.withType<JavaExec>().matching { it.name == "runServer" }.configureEach {
    standardInput = System.`in`
}

neoForge {
    version = property("deps.neoforge_loader") as String
    addModdingDependenciesTo(sourceSets.test.get())
    runs {
        register("client") {
            client()
            gameDirectory = layout.projectDirectory.dir("runs/client")
        }
        register("server") {
            server()
            programArgument("--nogui")
            gameDirectory = layout.projectDirectory.dir("runs/server")
        }
    }
    mods.register(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }
    unitTest {
        enable()
        testedMod.set(mods.getByName(property("mod.id") as String))
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.processResources {
    inputs.file(project.buildFile) // Copy-spec expansion changes must invalidate previously processed resources.
    val props = mapOf(
        "id" to project.property("mod.id"),
        "name" to project.property("mod.name"),
        "version" to project.version.toString(),
        "license" to project.property("mod.license"),
        "authors" to project.property("mod.authors"),
        "description" to project.property("mod.description"),
        "minecraft" to project.property("mod.mc_compat"),
        "patchouli" to project.property("deps.patchouli"),
        "java" to 21,
        "pack_resource" to 34,
        "pack_data" to 48,
        "result_key" to "id",
        "obj_loader" to "neoforge",
        "silk_predicate" to """{"predicates":{"minecraft:enchantments":[{"enchantments":"minecraft:silk_touch","levels":{"min":1}}]}}""",
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) { expand(props) }
    filesMatching("arcanearchives.mixins.json") { expand(props) }
    filesMatching("data/arcanearchives/recipe/*.json") { expand(props) }
    filesMatching("data/arcanearchives/advancement/*.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/gemcutters_table.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/wonky_resonator.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/celestial_lotus_engine.json") { expand(props) }
    filesMatching(listOf("verdant_censer", "echoing_conformance_chamber", "echoing_reverberation_chamber").map { "assets/arcanearchives/models/block/$it.json" }) { expand(props) }
    filesMatching("assets/arcanearchives/models/block/radiant_lantern.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/monitoring_crystal.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/quartz_sliver.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/raw_quartz_cluster.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/radiant_trove.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/radiant_tank.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/radiant_crafting_table.json") { expand(props) }
    filesMatching(listOf("assets/arcanearchives/models/block/radiant_resonator.json", "assets/arcanearchives/models/block/radiant_chest.json")) { expand(props) }
    filesMatching("data/arcanearchives/loot_table/blocks/raw_quartz_cluster.json") {
        filter { line: String -> line.replace("\"@SILK_PREDICATE@\"", props.getValue("silk_predicate").toString()) }
    }
    exclude("fabric.mod.json", "arcanearchives.fabric.mixins.json", "arcanearchives.forge.mixins.json", "META-INF/mods.toml")
    exclude("data/trinkets/**")
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}
