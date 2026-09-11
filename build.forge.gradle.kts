plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.143"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-forge"

// Test-only mod: reuse the shared assertions inside Forge's transformed server runtime.
val gameTest = sourceSets.create("gameTest") {
    java.srcDir(rootProject.file("src/sharedGameTest/java"))
    java.srcDir(rootProject.file("src/forgeGameTest/java"))
    resources.srcDir(rootProject.file("src/forgeGameTest/resources"))
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}
configurations[gameTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[gameTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

repositories {
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.blamejared.com/") { content { includeGroup("vazkii.patchouli") } }
}

dependencies {
    // Development-only opt-in; never bundled or required in player metadata.
    val optionalIntegrationMods = providers.gradleProperty("optionalIntegrationMods").orElse("").get()
        .split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    require(optionalIntegrationMods.all { it in setOf("jade", "jei", "emi", "kubejs") }) {
        "optionalIntegrationMods supports jade,jei,emi,kubejs only"
    }
    optionalIntegrationMods.forEach { mod -> add("modRuntimeOnly", "maven.modrinth:$mod:${property("deps.$mod")}") }
    if ("kubejs" in optionalIntegrationMods) {
        add("modRuntimeOnly", "maven.modrinth:rhino:${property("deps.rhino")}")
        add("modRuntimeOnly", "maven.modrinth:architectury-api:${property("deps.architectury")}")
    }
    add("modCompileOnly", "maven.modrinth:jade:${property("deps.jade")}")
    add("modCompileOnly", "maven.modrinth:jei:${property("deps.jei")}")
    add("modCompileOnly", "maven.modrinth:emi:${property("deps.emi")}")
    add("modCompileOnly", "maven.modrinth:kubejs:${property("deps.kubejs")}")
    add("modCompileOnly", "maven.modrinth:curios:${property("deps.curios")}")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    add("modCompileOnly", "vazkii.patchouli:Patchouli:${property("deps.patchouli")}:api")
    add("modRuntimeOnly", "vazkii.patchouli:Patchouli:${property("deps.patchouli")}")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add(gameTest.implementationConfigurationName, "org.junit.platform:junit-platform-launcher")
    add(gameTest.implementationConfigurationName, "org.junit.jupiter:junit-jupiter-engine")
    add(gameTest.implementationConfigurationName, "org.junit.platform:junit-platform-reporting")
}

tasks.test {
    useJUnitPlatform()
    // These fixtures do not bootstrap Minecraft. All shared fixtures also run in the GameTest server.
    include("**/MathUtilsTest.class", "**/IngredientAllocationTest.class", "**/ClientConfigTest.class",
        "**/ServerSideConfigTest.class", "**/PlayerSaveDataTest.class", "**/HiveCraftingConditionsTest.class",
        "**/SliverSmashingTest.class")
}
tasks.named("check") { dependsOn("runGameTestServer") }

mixin {
    add(sourceSets.main.get(), "arcanearchives.refmap.json")
    config("arcanearchives.forge.mixins.json")
}

tasks.withType<JavaExec>().matching { it.name == "runServer" }.configureEach {
    standardInput = System.`in`
}

legacyForge {
    version = "${sc.current.version}-${property("deps.forge_loader")}"
    addModdingDependenciesTo(sourceSets.test.get())
    addModdingDependenciesTo(gameTest)
    mods.register(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }
    mods.register("arcanearchives_test") {
        sourceSet(sourceSets.main.get())
        sourceSet(gameTest)
        sourceSet(sourceSets.test.get())
    }
    runs {
        configureEach { loadedMods = listOf(mods.getByName(property("mod.id") as String)) }
        register("client") {
            client()
            gameDirectory = layout.projectDirectory.dir("runs/client")
        }
        register("server") {
            server()
            programArgument("--nogui")
            gameDirectory = layout.projectDirectory.dir("runs/server")
        }
        register("gameTestServer") {
            type = "gameTestServer"
            sourceSet = gameTest
            additionalRuntimeClasspathConfiguration.extendsFrom(configurations[gameTest.implementationConfigurationName])
            loadedMods = listOf(mods.getByName("arcanearchives_test"))
            gameDirectory = layout.buildDirectory.dir("gametest-run")
            systemProperty("forge.enabledGameTestNamespaces", "arcanearchives_test")
            systemProperty("arcanearchives.test.classes", sourceSets.test.get().output.classesDirs.asPath)
            systemProperty("arcanearchives.test.reports", layout.buildDirectory.dir("test-results/forge-runtime").get().asFile.absolutePath)
        }
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(17)
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
        "pack_resource" to 15,
        "pack_data" to 15,
        "result_key" to "item",
        "obj_loader" to "forge",
        "silk_predicate" to """{"enchantments":[{"enchantment":"minecraft:silk_touch","levels":{"min":1}}]}""",
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
    filesMatching("data/arcanearchives/recipe/*.json") { expand(props) }
    filesMatching("data/arcanearchives/advancement/*.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/gemcutters_table.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/wonky_resonator.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/celestial_lotus_engine.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/matrix_reservoir.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/matrix_distillate.json") { expand(props) }
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
    eachFile {
        path = path.replace("data/arcanearchives/recipe/", "data/arcanearchives/recipes/")
            .replace("data/arcanearchives/loot_table/", "data/arcanearchives/loot_tables/")
            .replace("data/arcanearchives/advancement/", "data/arcanearchives/advancements/")
            .replace("data/minecraft/tags/block/", "data/minecraft/tags/blocks/")
            .replace("data/arcanearchives/tags/item/", "data/arcanearchives/tags/items/")
    }
    exclude("fabric.mod.json", "arcanearchives.fabric.mixins.json", "arcanearchives.mixins.json", "META-INF/neoforge.mods.toml")
    exclude("data/trinkets/**")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    manifest.attributes("MixinConfigs" to "arcanearchives.forge.mixins.json")
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}

tasks.named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }

val prepareGameTestStructure = tasks.register<Copy>("prepareGameTestStructure") {
    from(rootProject.file("src/forgeGameTest/structures"))
    into(layout.buildDirectory.dir("gametest-run/gameteststructures"))
}
tasks.named("runGameTestServer") { dependsOn(prepareGameTestStructure) }
