package com.aranaira.arcanearchives.client;

//? if fabric {
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vazkii.patchouli.client.book.BookPage;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.template.TemplateComponent;
import static org.junit.jupiter.api.Assertions.*;

/** Installed Patchouli serializers on each Minecraft version, without a graphics context. */
class TomePageTypesTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void completeBookUsesRecognizedNativePagesAndComponents() throws Exception {
        var anchor = getClass().getResource("/assets/arcanearchives/patchouli_books/tome_arcana/en_us/categories/home.json");
        assertNotNull(anchor);
        Path root = Path.of(anchor.toURI()).getParent().getParent();
        int entries = 0;
        int components = 0;
        try (var paths = Files.walk(root)) {
            for (var path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (json.has("pages")) {
                    entries++;
                    var pages = ClientBookRegistry.INSTANCE.gson.fromJson(json.get("pages"), BookPage[].class);
                    assertEquals(json.getAsJsonArray("pages").size(), pages.length);
                    for (var page : pages) assertNotNull(page, path.toString());
                }
                if (json.has("components")) {
                    for (var component : json.getAsJsonArray("components")) {
                        assertNotNull(ClientBookRegistry.INSTANCE.gson.fromJson(component, TemplateComponent.class), path.toString());
                        components++;
                    }
                }
            }
        }
        assertEquals(73, entries);
        assertEquals(222, components, "Must parse the complete templates, not only recipe prototypes");
    }
}
//?}
