package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipeList;
import com.aranaira.arcanearchives.recipe.gct.GemCutterCraftingState;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Server-owned recipe selection and immediate paid crafting; ghost slots are never native outputs. */
public final class GemCuttersTableMenu extends AbstractContainerMenu {
    private final Container inputs;
    private final Player owner;
    private final Inventory playerInventory;
    private final Container display = new SimpleContainer(8);
    private final GCTRecipeList catalog;
    private ResourceLocation selected;
    private int page;
    private final Supplier<GemCutterCraftingState> craftingState;
    private final BooleanSupplier craftAccess;
    private final Predicate<ItemStack> needsRemainders;
    private final java.util.function.Function<ItemStack, java.util.Optional<ItemStack>> fluidRemainder;
    private boolean committing;
    private final Supplier<List<GemCutterDataRecipe.Entry>> recipeSource;
    private List<GemCutterDataRecipe.Entry> loadedRecipes;

    public GemCuttersTableMenu(int id, Inventory inventory) {
        this(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), id, inventory, new SimpleContainer(18), null);
    }

    public GemCuttersTableMenu(int id, Inventory inventory, Container inputs) {
        this(id, inventory, inputs, null, () -> false);
    }

    public GemCuttersTableMenu(int id, Inventory inventory, Container inputs,
            Supplier<GemCutterCraftingState> craftingState, BooleanSupplier craftAccess) {
        this(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), id, inventory, inputs,
            new GCTRecipeList(), craftingState, craftAccess, GemCuttersTableMenu::needsRemainderProcessing,
            () -> GemCutterDataRecipe.entries(inventory.player.level().getRecipeManager()));
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs) {
        this(type, id, inventory, inputs, new GCTRecipeList());
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs, GCTRecipeList catalog) {
        this(type, id, inventory, inputs, catalog, null, () -> false);
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs, GCTRecipeList catalog,
            Supplier<GemCutterCraftingState> craftingState, BooleanSupplier craftAccess) {
        this(type, id, inventory, inputs, catalog, craftingState, craftAccess, GemCuttersTableMenu::needsRemainderProcessing);
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs, GCTRecipeList catalog,
            Supplier<GemCutterCraftingState> craftingState, BooleanSupplier craftAccess, Predicate<ItemStack> needsRemainders) {
        this(type, id, inventory, inputs, catalog, craftingState, craftAccess, needsRemainders, null);
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs, GCTRecipeList catalog,
            Supplier<GemCutterCraftingState> craftingState, BooleanSupplier craftAccess, Predicate<ItemStack> needsRemainders,
            Supplier<List<GemCutterDataRecipe.Entry>> recipeSource) {
        this(type, id, inventory, inputs, catalog, craftingState, craftAccess, needsRemainders, recipeSource,
            com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders::prepare);
    }

    GemCuttersTableMenu(MenuType<?> type, int id, Inventory inventory, Container inputs, GCTRecipeList catalog,
            Supplier<GemCutterCraftingState> craftingState, BooleanSupplier craftAccess, Predicate<ItemStack> needsRemainders,
            Supplier<List<GemCutterDataRecipe.Entry>> recipeSource,
            java.util.function.Function<ItemStack, java.util.Optional<ItemStack>> fluidRemainder) {
        super(type, id);
        checkContainerSize(inputs, 18);
        this.inputs = inputs;
        this.owner = inventory.player;
        this.playerInventory = inventory;
        this.catalog = catalog;
        this.craftingState = craftingState;
        this.craftAccess = craftAccess;
        this.needsRemainders = needsRemainders;
        this.fluidRemainder = fluidRemainder;
        this.recipeSource = recipeSource;
        if (catalog != null && catalog.size() > 0) selected = catalog.getRecipeByIndex(0).getName();
        addSlot(new UnavailableSlot(display, 0, 95, 18));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 23 + column * 18, 166 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 23 + column * 18, 224));
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inputs, row * 9 + column, 23 + column * 18, 105 + row * 18));
            }
        }
        for (int column = 6; column >= 0; column--) {
            addSlot(new UnavailableSlot(display, column + 1, column * 18 + 41, 70));
        }
        updateRecipeDisplay();
    }

    private List<com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe.Entry> availableRecipes() {
        return recipeSource.get().stream().filter(entry -> entry.recipe().enabledFor(owner)).toList();
    }

    private boolean refreshRecipes() {
        if (recipeSource == null || committing) return false;
        committing = true;
        try {
            var current = availableRecipes();
            if (current.equals(loadedRecipes)) return false;
            boolean initial = loadedRecipes == null;
            catalog.replaceAll(current.stream().filter(entry -> entry.recipe().enabled())
                .map(entry -> entry.recipe().definition(entry.name())).toList());
            loadedRecipes = current;
            if (initial && catalog.size() > 0) selected = catalog.getRecipeByIndex(0).getName();
            if (page >= catalog.pageCount()) page = 0;
            return true;
        } finally {
            committing = false;
        }
    }

    private void updateRecipeDisplay() {
        if (catalog == null || committing) return; // Client only receives native slot synchronization.
        refreshRecipes();
        committing = true;
        try {
            if (page >= catalog.pageCount()) page = 0;
            List<GCTRecipe> definitions = catalog.getRecipePage(page);
            for (int index = 0; index < 7; index++) {
                display.setItem(index + 1, index < definitions.size() ? definitions.get(index).getRecipeOutput() : ItemStack.EMPTY);
            }
            GCTRecipe recipe = selected == null ? null : catalog.getRecipe(selected);
            List<ItemStack> combined = new ArrayList<>(54);
            for (int slot = 0; slot < 18; slot++) combined.add(inputs.getItem(slot).copy());
            for (int slot = 0; slot < 36; slot++) combined.add(playerInventory.getItem(slot).copy());
            display.setItem(0, stillValid(owner) && recipe != null && recipe.matches(combined)
                ? recipe.getRecipeOutput() : ItemStack.EMPTY);
        } finally {
            committing = false;
        }
    }

    @Override
    public void broadcastChanges() {
        if (committing) return;
        updateRecipeDisplay();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (committing || catalog == null || !stillValid(player) || (button != 0 && button != 1)) return false;
        if (refreshRecipes()) { broadcastChanges(); return false; }
        page = button == 0 ? catalog.previousPage(page) : catalog.nextPage(page);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && inputs.stillValid(player);
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (committing || !stillValid(player) || slot >= slots.size() || (slot < -1 && slot != SLOT_CLICKED_OUTSIDE)) return;
        if (slot == 0 || slot >= 55) {
            if (slot == 0 && (button == 0 || button == 1) && (type == ClickType.PICKUP || type == ClickType.QUICK_MOVE)) {
                craftOutput(type == ClickType.QUICK_MOVE);
                broadcastChanges();
                return;
            }
            if (catalog != null && slot >= 55 && type == ClickType.PICKUP && (button == 0 || button == 1)) {
                if (refreshRecipes()) { broadcastChanges(); return; }
                List<GCTRecipe> definitions = catalog.getRecipePage(page);
                int index = 61 - slot;
                if (index < definitions.size()) selected = definitions.get(index).getName();
                broadcastChanges();
            }
            return; // Ghost items must never reach native clone, swap, throw or extraction handling.
        }
        if (slot >= 0 && !slots.get(slot).isActive()) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (committing || !stillValid(player)) return ItemStack.EMPTY;
        if (index == 0) return craftOutput(true);
        if (index < 1 || index >= 55) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.mayPickup(player) || !source.hasItem()) return ItemStack.EMPTY;
        ItemStack original = source.getItem().copy();
        ItemStack remainder = original.copy();
        int start = index < 37 ? 37 : 1;
        int end = index < 37 ? 55 : 37;
        boolean reverse = index >= 37;
        // safeInsert writes back merged copies, unlike vanilla moveItemStackTo's in-place merge.
        for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
            for (int offset = 0; offset < end - start && !remainder.isEmpty(); offset++) {
                Slot destination = slots.get(reverse ? end - 1 - offset : start + offset);
                if (destination.hasItem() == (pass == 0)) destination.safeInsert(remainder);
            }
        }
        if (remainder.getCount() == original.getCount()) return ItemStack.EMPTY;
        source.setByPlayer(remainder);
        ItemStack moved = original.copy();
        moved.setCount(original.getCount() - remainder.getCount());
        source.onTake(player, moved);
        return original;
    }

    private boolean canCraft() {
        return craftingState != null && catalog != null && selected != null && craftAccess.getAsBoolean()
            && stillValid(owner) && (owner == null || owner.containerMenu == this);
    }

    private ItemStack craftOutput(boolean toInventory) {
        if (committing) return ItemStack.EMPTY;
        if (refreshRecipes()) return ItemStack.EMPTY;
        committing = true;
        try {
            if (!canCraft()) return ItemStack.EMPTY;
            GemCutterCraftingState state = craftingState.get();
            if (state.pendingResult().isPresent()) return ItemStack.EMPTY;
            BooleanSupplier unchanged = catalog.unchanged();
            GCTRecipe recipe = catalog.getRecipe(selected);
            if (recipe == null) return ItemStack.EMPTY;
            var tags = recipe.getIngredients().stream().map(IngredientStack::getTagItems).toList();
            List<ItemStack> tableBefore = state.inputSnapshot();
            List<ItemStack> playerBefore = playerInventory.items.stream().map(ItemStack::copy).toList();
            if (playerBefore.size() != 36) return ItemStack.EMPTY;
            ItemStack cursorBefore = getCarried().copy();
            List<ItemStack> combined = new ArrayList<>(tableBefore);
            combined.addAll(playerBefore);
            for (ItemStack stack : combined) {
                if (!stack.isEmpty() && stack.getCount() > Math.min(64, stack.getMaxStackSize())) return ItemStack.EMPTY;
            }
            var allocation = recipe.getMatchingSlots(combined);
            if (allocation.isEmpty()) return ItemStack.EMPTY;
            List<ItemStack> remaining = new ArrayList<>(54);
            List<ItemStack> returns = new ArrayList<>();
            for (int slot = 0; slot < combined.size(); slot++) {
                ItemStack stack = combined.get(slot).copy();
                if (allocation.get()[slot] > 0 && needsRemainders.test(stack.copyWithCount(1))) {
                    for (int unit = 0; unit < allocation.get()[slot]; unit++) {
                        var consumed = stack.copyWithCount(1);
                        var prepared = consumed.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem
                            ? prepareFlint(consumed, owner != null && owner.getAbilities().instabuild)
                            : fluidRemainder.apply(consumed);
                        if (prepared.isEmpty()) {
                            if (owner != null) owner.displayClientMessage(Component.translatable("arcanearchives.gem_cutter.remainders_pending"), true);
                            return ItemStack.EMPTY;
                        }
                        returns.add(prepared.get().copy());
                    }
                }
                stack.shrink(allocation.get()[slot]);
                remaining.add(stack);
            }
            for (ItemStack returned : returns) {
                if (!insertStack(remaining.subList(0, 18), returned, false)
                        && !insertStack(remaining.subList(18, 54), returned, false)) return ItemStack.EMPTY;
            }
            // The null owner exists only in headless container fixtures; live menus use server identity.
            ItemStack output = owner == null ? recipe.getRecipeOutput()
                : recipe.createOutput(owner.getUUID(), owner.getName().getString());
            ItemStack cursorAfter = cursorBefore.copy();
            if (toInventory) {
                if (!insertOutput(remaining.subList(18, 54), output.copy())) return ItemStack.EMPTY;
            } else {
                if (!cursorAfter.isEmpty() && !ExtendedItemStackHandler.sameItemAndData(cursorAfter, output)) return ItemStack.EMPTY;
                if (output.getCount() > Math.min(64, output.getMaxStackSize()) - cursorAfter.getCount()) return ItemStack.EMPTY;
                cursorAfter = output.copyWithCount(cursorAfter.getCount() + output.getCount());
            }
            if (!state.commitCraftInputs(tableBefore, remaining.subList(0, 18), () -> canCraft()
                    && craftingState.get() == state && unchanged.getAsBoolean()
                    && (recipeSource == null || loadedRecipes.equals(availableRecipes()))
                    && tags.equals(recipe.getIngredients().stream().map(IngredientStack::getTagItems).toList())
                    && sameStacks(playerBefore, playerInventory.items) && sameStack(cursorBefore, getCarried()))) return ItemStack.EMPTY;
            // No external hooks between replacing owned table state and these native player/cursor writes.
            for (int slot = 0; slot < 36; slot++) playerInventory.items.set(slot, remaining.get(slot + 18));
            if (!toInventory) setCarried(cursorAfter);
            playerInventory.setChanged();
            inputs.setChanged();
            return output;
        } finally {
            committing = false;
        }
    }

    static java.util.Optional<ItemStack> prepareFlint(ItemStack consumed, boolean creative) {
        if (!consumed.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)
                || consumed.getCount() != 1 || consumed.isEnchanted()) return java.util.Optional.empty();
        ItemStack result = consumed.copy();
        if (!creative && result.isDamageableItem()) {
            if (result.getDamageValue() >= result.getMaxDamage() - 1) result = ItemStack.EMPTY;
            else result.setDamageValue(result.getDamageValue() + 1);
        }
        return java.util.Optional.of(result);
    }

    private static boolean insertOutput(List<ItemStack> inventory, ItemStack output) {
        return insertStack(inventory, output, true);
    }

    private static boolean insertStack(List<ItemStack> inventory, ItemStack output, boolean reversePlayerOrder) {
        for (int pass = 0; pass < 2 && !output.isEmpty(); pass++) {
            for (int offset = 0; offset < inventory.size() && !output.isEmpty(); offset++) {
                int slot = reversePlayerOrder ? (offset < 9 ? 8 - offset : 44 - offset) : offset;
                ItemStack existing = inventory.get(slot);
                if (existing.isEmpty() != (pass == 1)
                        || !existing.isEmpty() && !ExtendedItemStackHandler.sameItemAndData(existing, output)) continue;
                int amount = Math.min(output.getCount(), Math.min(64, output.getMaxStackSize()) - existing.getCount());
                if (amount <= 0) continue;
                inventory.set(slot, output.copyWithCount(existing.getCount() + amount));
                output.shrink(amount);
            }
        }
        return output.isEmpty();
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount() && ExtendedItemStackHandler.sameItemAndData(first, second);
    }

    private static boolean sameStacks(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) return false;
        for (int slot = 0; slot < first.size(); slot++) if (!sameStack(first.get(slot), second.get(slot))) return false;
        return true;
    }

    private static boolean needsRemainderProcessing(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem) return true;
        //? if fabric {
        return !stack.copy().getRecipeRemainder().isEmpty()
            || net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM.find(stack,
                net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(stack)) != null;
        //?} else if forge {
        /*return stack.hasCraftingRemainingItem()
            || stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
        *///?} else {
        /*return stack.hasCraftingRemainingItem()
            || stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM) != null;
        *///?}
    }

    private static final class UnavailableSlot extends Slot {
        private UnavailableSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) { return false; }

        @Override
        public boolean mayPickup(Player player) { return false; }

        @Override
        public boolean isActive() { return hasItem(); }
    }
}
