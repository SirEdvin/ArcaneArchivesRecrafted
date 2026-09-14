plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id")}-fabric"
val requiredJava = if (sc.current.parsed >= "1.20.5") 21 else 17

repositories {
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.ladysnake.org/releases") { content { includeGroup("dev.onyxstudios.cardinal-components-api"); includeGroup("org.ladysnake.cardinal-components-api") } }
    maven("https://maven.blamejared.com/") { content { includeGroup("vazkii.patchouli") } }
    maven("https://maven.modmuss50.me/") { content { includeGroup("me.zeroeightsix") } }
}

dependencies {
    // Development-only opt-in; never bundled or required in player metadata.
    val optionalIntegrationMods = providers.gradleProperty("optionalIntegrationMods").orElse("").get()
        .split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    require(optionalIntegrationMods.all { it in setOf("jade", "jei", "emi", "kubejs") }) {
        "optionalIntegrationMods supports jade,jei,emi,kubejs only"
    }
    // Gradle configures every leaf: the approved 1.21 Fabric exception applies here too.
    optionalIntegrationMods.filter { it != "kubejs" || sc.current.parsed < "1.21" }
        .forEach { mod -> modRuntimeOnly("maven.modrinth:$mod:${property("deps.$mod")}") }
    if ("kubejs" in optionalIntegrationMods && sc.current.parsed < "1.21") {
        // KubeJS injects Rhino interfaces into vanilla classes in Loom's compile view.
        modImplementation("maven.modrinth:rhino:${property("deps.rhino")}")
        modRuntimeOnly("maven.modrinth:architectury-api:${property("deps.architectury")}")
    }
    modCompileOnly("maven.modrinth:jade:${property("deps.jade")}")
    modCompileOnly("maven.modrinth:jei:${property("deps.jei")}")
    modCompileOnly("maven.modrinth:emi:${property("deps.emi")}")
    if (sc.current.parsed < "1.21") modCompileOnly("maven.modrinth:kubejs:${property("deps.kubejs")}")
    modCompileOnly("maven.modrinth:trinkets:${property("deps.trinkets")}")
    modCompileOnly("${property("deps.cca_group")}:cardinal-components-base:${property("deps.cca")}")
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("vazkii.patchouli:Patchouli:${property("deps.patchouli")}")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

fabricApi.configureTests {
    createSourceSet = true
    modId = "arcanearchives_test"
    enableGameTests = true
    enableClientGameTests = false
    eula = false
    clearRunDirectory = false
}
sourceSets.named("gametest") {
    java.srcDir(rootProject.file("src/sharedGameTest/java"))
    java.srcDir(rootProject.file("src/fabricGameTest/java"))
    java.srcDir(rootProject.file("src/fabricGameTest/${sc.current.version}"))
    resources.srcDir(rootProject.file("src/fabricGameTest/resources"))
}
tasks.named("runGameTest") {
    val report = layout.buildDirectory.file("gametest-run/results.xml").get().asFile
    var startedAt = 0L
    doFirst { startedAt = System.currentTimeMillis() }
    doLast {
        check(report.isFile && report.lastModified() >= startedAt) { "Missing fresh Fabric GameTest report: $report" }
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        val result = factory.newDocumentBuilder().parse(report)
        val cases = result.getElementsByTagName("testcase")
        val names = (0 until cases.length).map { (cases.item(it) as org.w3c.dom.Element).getAttribute("name") }
        val required = listOf("matrixreservoirplacement", "matrixreservoirinterruptedplacement", "matrixreservoirplacementpreflight", "matrixreservoircrafting", "matrixdistillatelifecycle", "deviceownership")
        check(required.all { test -> names.count { it == "fabricruntimetests.$test" } == 1 } &&
            listOf("failure", "error", "skipped").all { result.getElementsByTagName(it).length == 0 }) {
            "Fabric GameTests did not complete every required Reservoir fixture successfully: $report"
        }
    }
}

tasks.withType<JavaExec>().matching { it.name == "runServer" }.configureEach {
    standardInput = System.`in`
}

loom {
    runs {
        named("client") { runDir("runs/client") }
        named("server") { runDir("runs/server") }
        named("gameTest") {
            runDir("build/gametest-run")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory.file("gametest-run/results.xml").get().asFile.absolutePath}")
        }
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava)
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
        "java" to requiredJava,
        "pack_resource" to if (sc.current.parsed >= "1.21") 34 else 15,
        "pack_data" to if (sc.current.parsed >= "1.21") 48 else 15,
        "result_key" to if (sc.current.parsed >= "1.21") "id" else "item",
        "brazier_fog" to if (sc.current.parsed >= "1.21") "fog_distance(Position, FogShape)" else "fog_distance(ModelViewMat, IViewRotMat * Position, FogShape)",
        "obj_loader" to "forge", // Ignored by vanilla JSON; the Fabric client supplies the OBJ adapter.
        "silk_predicate" to if (sc.current.parsed >= "1.21") """{"predicates":{"minecraft:enchantments":[{"enchantments":"minecraft:silk_touch","levels":{"min":1}}]}}"""
            else """{"enchantments":[{"enchantment":"minecraft:silk_touch","levels":{"min":1}}]}""",
    )
    inputs.properties(props)
    filesMatching("assets/arcanearchives/shaders/core/brazier_fire.vsh") {
        expand(props)
    }
    if (sc.current.parsed >= "1.21") filesMatching("assets/arcanearchives/shaders/core/brazier_fire.json") {
        filter { line: String -> if (line.contains("IViewRotMat") || line.contains("\"blend\":")) "" else line }
    }
    filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) { expand(props) }
    filesMatching(listOf("arcanearchives.fabric.mixins.json", "arcanearchives.mixins.json")) { expand(props) }
    filesMatching("data/arcanearchives/recipe/*.json") { expand(props) }
    filesMatching("data/arcanearchives/advancement/*.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/gemcutters_table.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/lectern_manifest.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/brazier_of_hoarding_fire.json") { expand(props) }
    filesMatching("assets/arcanearchives/models/block/brazier_of_hoarding.json") { expand(props) }
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
    if (sc.current.parsed < "1.21") {
        eachFile {
            path = path.replace("data/arcanearchives/recipe/", "data/arcanearchives/recipes/")
                .replace("data/arcanearchives/loot_table/", "data/arcanearchives/loot_tables/")
                .replace("data/arcanearchives/advancement/", "data/arcanearchives/advancements/")
                .replace("data/minecraft/tags/block/", "data/minecraft/tags/blocks/")
                .replace("data/arcanearchives/tags/block/", "data/arcanearchives/tags/blocks/")
                .replace("data/arcanearchives/tags/item/", "data/arcanearchives/tags/items/")
        }
    }
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "arcanearchives.forge.mixins.json")
    exclude("data/arcanearchives/curios/**")
}

group = property("mod.group") as String

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("docs/upstream/LICENSE")) { into("META-INF/upstream") }
}
