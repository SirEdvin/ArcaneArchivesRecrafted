package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManifestSearchTest {
    @Test void enchantmentNamesIncludeLevelsButNeverModQueries() {
        assertTrue(ManifestSearch.matchesEnchantmentName("SHARPNESS", "Sharpness V"));
        assertTrue(ManifestSearch.matchesEnchantmentName("sharpness v", "Sharpness V"));
        assertFalse(ManifestSearch.matchesEnchantmentName("sharpness iv", "Sharpness V"));
        assertFalse(ManifestSearch.matchesEnchantmentName("@sharpness", "Sharpness V"));
        assertFalse(ManifestSearch.matchesEnchantmentName("mending", "Sharpness V"));
    }

    @Test void registryFallbacksPreserveOriginalSearchBoundaries() {
        assertTrue(ManifestSearch.matches("diamond", "Family Heirloom", "diamond_sword", "Minecraft", "minecraft"));
        assertTrue(ManifestSearch.matches("HEIRLOOM", "Family Heirloom", "diamond_sword", "Minecraft", "minecraft"));
        assertTrue(ManifestSearch.matches("@aa", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertTrue(ManifestSearch.matches("@ARCANEARCH", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertFalse(ManifestSearch.matches("@trove", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertFalse(ManifestSearch.matches("arcane", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertFalse(ManifestSearch.matches("@arcane archives", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertTrue(ManifestSearch.matches("", "Trove", "radiant_trove", "Arcane Archives", "aa"));
        assertTrue(ManifestSearch.matches("@", "Trove", "radiant_trove", "Arcane Archives", "aa"));
    }

    @BeforeEach @AfterEach void reset() {
        ManifestSearch.unbind();
        new ManifestSearch(false, false).close();
    }

    @Test void absentJeiLeavesSearchFunctionalAndOptional() {
        var search = new ManifestSearch(false, true);
        assertFalse(search.available());
        search.edit("diamond");
        assertEquals("diamond", search.query());
        assertTrue(search.copyFromJei().isEmpty());
        assertDoesNotThrow(search::toggle);
        assertDoesNotThrow(search::close);
    }

    @Test void enablingSyncPushesCurrentQueryAndEditsIncludeClearing() {
        var jei = new AtomicReference<>("original");
        var writes = new ArrayList<String>();
        ManifestSearch.bind(jei::get, value -> { jei.set(value); writes.add(value); });
        var search = new ManifestSearch(false, false);
        search.edit("iron");
        assertTrue(writes.isEmpty());
        search.toggle();
        assertEquals("iron", jei.get());
        search.edit("");
        assertEquals("", jei.get());
        search.close();
        assertEquals(java.util.List.of("iron", "", "original"), writes);
        search.close();
        assertEquals(3, writes.size(), "Repeated removal must not restore twice");
    }

    @Test void copyingJeiWorksWithoutEnablingSynchronization() {
        var jei = new AtomicReference<>("@Minecraft");
        ManifestSearch.bind(jei::get, jei::set);
        var search = new ManifestSearch(false, false);
        search.edit(search.copyFromJei().orElseThrow());
        assertEquals("@Minecraft", search.query());
        jei.set("changed externally");
        search.close();
        assertEquals("changed externally", jei.get());
    }

    @Test void disabledOnCloseDoesNotRestoreAndEmptyOriginalStaysUnrestored() {
        var jei = new AtomicReference<>("original");
        ManifestSearch.bind(jei::get, jei::set);
        var search = new ManifestSearch(false, true);
        search.edit("diamond");
        search.toggle();
        search.close();
        assertEquals("diamond", jei.get());
        jei.set("");
        search = new ManifestSearch(false, true);
        search.edit("gold");
        search.close();
        assertEquals("gold", jei.get(), "Preserve the upstream nonempty restoration condition");
    }

    @Test void openingDoesNotPushUntilAnEditAndPersistenceIsOptIn() {
        var first = new ManifestSearch(false, false);
        first.edit("quartz");
        first.close();
        var jei = new AtomicReference<>("JEI prior");
        ManifestSearch.bind(jei::get, jei::set);
        var retained = new ManifestSearch(true, true);
        assertEquals("quartz", retained.query());
        assertEquals("JEI prior", jei.get());
        retained.close();
        assertEquals("", new ManifestSearch(false, false).query());
    }

    @Test void runtimeUnloadAndReplacementInvalidateOldCallbacks() {
        var old = new AtomicReference<>("old");
        ManifestSearch.bind(old::get, old::set);
        var search = new ManifestSearch(false, true);
        ManifestSearch.unbind();
        assertFalse(search.available());
        search.edit("ignored");
        assertEquals("old", old.get());
        var replacement = new AtomicReference<>("new runtime");
        ManifestSearch.bind(replacement::get, replacement::set);
        assertFalse(search.available());
        assertTrue(search.copyFromJei().isEmpty());
        search.close();
        assertEquals("old", old.get());
        assertEquals("new runtime", replacement.get());
        var reopened = new ManifestSearch(true, true);
        assertTrue(reopened.available());
        reopened.edit("emerald");
        reopened.close();
        assertEquals("new runtime", replacement.get());
    }

    @Test void closedSessionCannotWriteOrOverwriteRememberedSearch() {
        var jei = new AtomicReference<>("original");
        ManifestSearch.bind(jei::get, jei::set);
        var search = new ManifestSearch(false, true);
        search.edit("quartz");
        search.close();
        assertTrue(search.closed());
        search.edit("late event");
        search.toggle();
        search.close();
        assertEquals("original", jei.get());
        assertEquals("quartz", new ManifestSearch(true, false).query());
    }
}
