package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.IngredientAllocation;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** Every viewer works on the same persistent grid; recipe icons never contain claimable items. */
public final class RadiantCraftingMenu extends AbstractContainerMenu {
    private final RadiantCraftingTableBlockEntity table;
    private final Inventory inventory;
    private final CraftingContainer matrix;
    private final ResultContainer result = new ResultContainer();
    private final SimpleContainer bookmarks = new SimpleContainer(3);
    private ResourceLocation current;
    private ResourceLocation lastCrafted;

    public RadiantCraftingMenu(int id, Inventory inventory) { this(id, inventory, null); }
    public RadiantCraftingMenu(int id, Inventory inventory, RadiantCraftingTableBlockEntity table) {
        super(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), id);
        this.table = table;
        this.inventory = inventory;
        matrix = new TransientCraftingContainer(this, 3, 3,
                table == null ? NonNullList.withSize(9, ItemStack.EMPTY) : table.items()) {
            @Override public void setChanged() { slotsChanged(this); }
        };
        addSlot(new ResultSlot(inventory.player, matrix, result, 0, 136, 42) {
            @Override public void onTake(Player player, ItemStack stack) {
                ResourceLocation crafted = current;
                super.onTake(player, stack);
                lastCrafted = crafted;
            }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
            addSlot(new Slot(matrix, row * 3 + col, 24 + col * 18, 24 + row * 18));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, 9 + row * 9 + col, 23 + col * 18, 115 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 23 + col * 18, 173));
        for (int index = 0; index < 3; index++) addSlot(new Slot(bookmarks, index, 174, 16 + index * 26) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });
        refresh();
    }

    @Override public boolean stillValid(Player player) { return table == null || table.canUse(player); }
    @Override public void slotsChanged(Container container) {
        if (table != null) table.setChanged();
        refresh();
    }
    private void refresh() {
        if (table == null || !(inventory.player instanceof ServerPlayer player)) return;
        ItemStack output = ItemStack.EMPTY;
        current = null;
        if (table.canUse(player)) {
            var level = player.level();
            //? if >=1.21 {
            var input = matrix.asCraftInput();
            var match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
            if (match != null && result.setRecipeUsed(level, player, match)) {
                output = match.value().assemble(input, level.registryAccess());
                current = match.id();
            }
            //?} else {
            /*var match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, matrix, level).orElse(null);
            if (match != null && result.setRecipeUsed(level, player, match)) {
                output = match.assemble(matrix, level.registryAccess());
                current = match.getId();
            }
            *///?}
            if (!output.isItemEnabled(level.enabledFeatures())) output = ItemStack.EMPTY;
        }
        if (output.isEmpty()) current = null;
        result.setItem(0, output);
        for (int index = 0; index < 3; index++) {
            CraftingRecipe recipe = recipe(table.recipe(index));
            bookmarks.setItem(index, recipe == null ? ItemStack.EMPTY : recipe.getResultItem(player.level().registryAccess()).copy());
        }
    }
    private CraftingRecipe recipe(ResourceLocation id) {
        if (id == null) return null;
        var saved = inventory.player.level().getRecipeManager().byKey(id).orElse(null);
        //? if >=1.21 {
        return saved != null && saved.value() instanceof CraftingRecipe crafting ? crafting : null;
        //?} else {
        /*return saved instanceof CraftingRecipe crafting ? crafting : null;
        *///?}
    }
    @Override public void broadcastChanges() { refresh(); super.broadcastChanges(); }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || slot >= 46) return;
        refresh();
        super.clicked(slot, button, type, player);
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && slot.container != bookmarks && super.canTakeItemForPickAll(stack, slot);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= 46 || !stillValid(player)) return ItemStack.EMPTY;
        refresh();
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        if (index < 10) {
            if (!moveItemStackTo(moving, 10, 46, true)) return ItemStack.EMPTY;
            if (index == 0) slot.onQuickCraft(moving, original);
        } else if (!moveItemStackTo(moving, 1, 10, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, moving);
        if (index == 0 && !moving.isEmpty()) player.drop(moving, false);
        return original;
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (table == null || player != inventory.player || player.containerMenu != this
                || !table.canUse(player) || button < 0 || button >= 6) return false;
        refresh();
        int index = button % 3;
        ResourceLocation saved = table.recipe(index);
        if (saved == null) table.setRecipe(index, button >= 3 ? lastCrafted : current);
        else if (button >= 3) table.setRecipe(index, null);
        else craftSaved(player, saved);
        broadcastChanges();
        return true;
    }

    private void craftSaved(Player player, ResourceLocation id) {
        CraftingRecipe recipe = recipe(id);
        if (recipe == null) return;
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty() || ingredients.size() > 9 || !recipe.canCraftInDimensions(3, 3)) return;
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        List<Integer> requirements = new ArrayList<>();
        for (int index = 0; index < ingredients.size(); index++)
            if (!ingredients.get(index).isEmpty()) requirements.add(index);
        if (requirements.isEmpty()) return;
        List<ItemStack> snapshot = new ArrayList<>(45);
        for (int index = 0; index < 45; index++) snapshot.add(source(index).copy());
        int[] available = snapshot.stream().mapToInt(ItemStack::getCount).toArray();
        int[] consumed = new int[45];
        NonNullList<ItemStack> samples = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int requirement = 0; requirement < requirements.size(); requirement++) {
            int ingredientIndex = requirements.get(requirement);
            boolean found = false;
            final int next = requirement + 1;
            int[] remaining = new int[requirements.size() - next];
            Arrays.fill(remaining, 1);
            for (int slot = 0; slot < available.length; slot++) {
                if (available[slot] == 0 || !ingredients.get(ingredientIndex).test(snapshot.get(slot).copy())) continue;
                available[slot]--;
                boolean feasible = IngredientAllocation.allocate(available, remaining,
                    (s, i) -> ingredients.get(requirements.get(next + i)).test(snapshot.get(s).copy())).isPresent();
                if (feasible) {
                    consumed[slot]++;
                    ItemStack sample = snapshot.get(slot).copy();
                    sample.setCount(1);
                    samples.set(ingredientIndex / width * 3 + ingredientIndex % width, sample);
                    found = true;
                    break;
                }
                available[slot]++;
            }
            if (!found) return;
        }
        CraftingContainer prepared = new TransientCraftingContainer(this, 3, 3, samples);
        //? if >=1.21 {
        var input = prepared.asCraftInput();
        //?} else {
        /*var input = prepared;
        *///?}
        if (!recipe.matches(input, player.level())) return;
        ItemStack output = recipe.assemble(input, player.level().registryAccess()).copy();
        if (output.isEmpty() || !output.isItemEnabled(player.level().enabledFeatures())) return;
        if (!table.canUse(player) || recipe(id) != recipe) return;
        for (int slot = 0; slot < 45; slot++) if (!ItemStack.matches(source(slot), snapshot.get(slot))) return;
        ResultContainer paid = new ResultContainer();
        paid.setRecipeUsed(player.level().getRecipeManager().byKey(id).orElseThrow());
        paid.setItem(0, output);
        for (int slot = 0; slot < 45; slot++) if (consumed[slot] > 0) {
            ItemStack replacement = snapshot.get(slot).copy();
            replacement.shrink(consumed[slot]);
            if (slot < 9) table.items().set(slot, replacement); else inventory.setItem(slot - 9, replacement);
        }
        table.setChanged();
        inventory.setChanged();
        lastCrafted = id;
        // As upstream: pay into a detached matrix, then let the native result slot handle crafting returns.
        new ResultSlot(player, prepared, paid, 0, 0, 0).onTake(player, output);
        give(player, output);
        for (int slot = 0; slot < prepared.getContainerSize(); slot++) give(player, prepared.removeItemNoUpdate(slot));
    }
    private ItemStack source(int slot) { return slot < 9 ? table.items().get(slot) : inventory.getItem(slot - 9); }
    private static void give(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        player.getInventory().add(stack);
        if (!stack.isEmpty()) player.drop(stack, false);
    }
}
