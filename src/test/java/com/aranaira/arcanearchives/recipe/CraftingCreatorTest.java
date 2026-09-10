package com.aranaira.arcanearchives.recipe;

import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CraftingCreatorTest {
    private static final UUID CREATOR = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void stampsCopyWithoutChangingTemplateOrOtherData() {
        ItemStack template = new ItemStack(Items.PAPER, 3);
        //? if >=1.21 {
        CustomData.update(DataComponents.CUSTOM_DATA, template, tag -> tag.putString("unrelated", "keep"));
        template.set(DataComponents.CUSTOM_NAME, Component.literal("Letter template"));
        //?} else {
        /*template.getOrCreateTag().putString("unrelated", "keep");
        template.setHoverName(Component.literal("Letter template"));
        *///?}
        ItemStack result = CraftingCreator.withCreator(template, CREATOR, "A literal creator name");
        assertEquals(CREATOR, data(result).getUUID("creator"));
        assertEquals("A literal creator name", data(result).getString("creator_name"));
        assertEquals("keep", data(result).getString("unrelated"));
        assertEquals(template.getHoverName(), result.getHoverName());
        assertEquals(3, result.getCount());
        assertFalse(data(template).contains("creator"));
        result.setCount(1);
        assertEquals(3, template.getCount());
    }

    @Test
    void anotherCrafterReplacesOnlyResultIdentity() {
        ItemStack first = CraftingCreator.withCreator(new ItemStack(Items.PAPER), CREATOR, "First");
        ItemStack second = CraftingCreator.withCreator(first, OTHER, "Second");
        assertEquals(CREATOR, data(first).getUUID("creator"));
        assertEquals("First", data(first).getString("creator_name"));
        assertEquals(OTHER, data(second).getUUID("creator"));
        assertEquals("Second", data(second).getString("creator_name"));
    }

    @Test
    void stampedIdentitySurvivesNativeItemPersistence() {
        ItemStack original = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 2), CREATOR, "Name");
        //? if >=1.21 {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        ItemStack restored = ItemStack.parse(registries, original.save(registries)).orElseThrow();
        //?} else {
        /*ItemStack restored = ItemStack.of(original.save(new CompoundTag()));
        *///?}
        assertEquals(CREATOR, data(restored).getUUID("creator"));
        assertEquals("Name", data(restored).getString("creator_name"));
        assertEquals(2, restored.getCount());
    }

    @Test
    void invalidInputsNeverChangeTemplate() {
        ItemStack template = new ItemStack(Items.PAPER);
        assertThrows(IllegalArgumentException.class, () -> CraftingCreator.withCreator(ItemStack.EMPTY, CREATOR, "Name"));
        assertThrows(NullPointerException.class, () -> CraftingCreator.withCreator(template, null, "Name"));
        assertThrows(NullPointerException.class, () -> CraftingCreator.withCreator(template, CREATOR, null));
        assertFalse(data(template).contains("creator"));
    }

    private static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        *///?}
    }
}
