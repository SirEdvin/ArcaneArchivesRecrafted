package com.aranaira.arcanearchives.client;

//? if fabric {
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vazkii.patchouli.api.IVariable;
import static org.junit.jupiter.api.Assertions.*;

class GemCutterBookComponentTest {
    @BeforeAll
    static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    private static void setup(GemCutterBookComponent component, UnaryOperator<IVariable> lookup) {
        //? if >=1.21 {
        component.onVariablesAvailable(lookup, net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
            net.minecraft.core.registries.BuiltInRegistries.REGISTRY));
        //?} else {
        /*component.onVariablesAvailable(lookup);
        *///?}
        component.build(4, 20, 0);
    }

    private static IVariable variable(String value) {
        //? if >=1.21 {
        return IVariable.wrap(value, net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
            net.minecraft.core.registries.BuiltInRegistries.REGISTRY));
        //?} else {
        /*return IVariable.wrap(value);
        *///?}
    }

    @Test
    void packagedTemplatesDeserializeAndResolveThroughRealPatchouliVariables() throws Exception {
        var id = ResourceLocation.tryParse("arcanearchives:preview");
        var recipe = GemCutterDataRecipe.parse(id, JsonParser.parseString("""
            {"inputs":[{"item":"minecraft:diamond","count":3}],"result":{"item":"minecraft:paper"}}
            """).getAsJsonObject());
        var entries = List.of(new GemCutterDataRecipe.Entry(id, recipe));
        for (String selector : List.of("output", "recipe")) {
            String path = "/assets/arcanearchives/patchouli_books/tome_arcana/en_us/templates/gem_cutting_" + selector + ".json";
            try (var stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("components").get(0).getAsJsonObject();
                assertEquals(GemCutterBookComponent.class.getName(), json.get("class").getAsString());
                var nativeComponent = vazkii.patchouli.client.book.ClientBookRegistry.INSTANCE.gson.fromJson(
                    json, vazkii.patchouli.client.book.template.TemplateComponent.class);
                assertInstanceOf(vazkii.patchouli.client.book.template.component.ComponentCustom.class,
                    nativeComponent, "Patchouli must recognize the packaged component, not silently discard it");
                var component = new Gson().fromJson(json, GemCutterBookComponent.class);
                setup(component, value -> {
                    assertEquals("#" + selector, value.asString());
                    return variable(selector.equals("recipe") ? id.toString() : "minecraft:paper");
                });
                assertEquals(id, component.resolve(entries).getName());
                assertEquals(3, component.resolve(entries).getIngredients().get(0).getMatchingStacksWithSizes().get(0).getCount());
                assertNull(component.resolve(List.of()));
            }
        }
    }

    @Test
    void invalidOrAmbiguousSelectorsFailExplicitlyWithoutNormalizingIdentifiers() {
        for (String json : List.of("{}", "{\"output\":\"minecraft:paper\",\"recipe\":\"arcanearchives:test\"}",
                "{\"recipe\":\"Unqualified\"}", "{\"recipe\":\"arcanearchives:BadName\"}")) {
            var component = new Gson().fromJson(json, GemCutterBookComponent.class);
            assertThrows(IllegalArgumentException.class, () -> setup(component, UnaryOperator.identity()), json);
        }
    }
}
//?}
