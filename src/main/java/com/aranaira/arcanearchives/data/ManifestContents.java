package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Server-owned read-only Manifest inventory projection. Does not authorize crafting or extraction. */
public final class ManifestContents {
    public enum Range { IN_RANGE, OUT_OF_RANGE, OTHER_DIMENSION }

    public record Location(BlockPosDimension position, String description, long count) {}

    public record Entry(ItemStack stack, long count, Range range, List<Location> locations) {
        public Entry {
            stack = stack.copy();
            stack.setCount(1);
            locations = List.copyOf(locations);
        }
        @Override public ItemStack stack() { return stack.copy(); }
    }

    private final Player viewer;
    private final double rangeSquared;
    private final Map<Item, List<Group>> groups = new LinkedHashMap<>();
    private final Set<BlockPosDimension> monitored = new HashSet<>();
    private final Set<Object> monitoredHandlers = Collections.newSetFromMap(new IdentityHashMap<>());

    private ManifestContents(Player viewer, int maxDistance) {
        this.viewer = viewer;
        rangeSquared = (double) maxDistance * maxDistance;
    }

    public static List<Entry> collect(Player viewer, int maxDistance) {
        if (viewer.level().isClientSide || viewer.getServer() == null || !viewer.getServer().isSameThread())
            throw new IllegalStateException("Manifest collection requires its server player thread");
        if (maxDistance < 0) throw new IllegalArgumentException("Manifest distance must be nonnegative");
        var contents = new ManifestContents(viewer, maxDistance);
        for (BlockEntity device : StorageNetworks.visible(viewer.getServer(), viewer.getUUID(), false)) {
            var position = new BlockPosDimension(device.getBlockPos(), device.getLevel().dimension());
            if (device instanceof RadiantChestBlockEntity chest) {
                String description = chest.chestName().isEmpty() ? "Chest" : "Chest: " + chest.chestName();
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                    contents.add(chest.inventory().getStackInSlot(slot), position, description);
            } else if (device instanceof RadiantTroveBlockEntity trove) {
                contents.add(trove.inventory().getStackInSlot(0), position, "Trove");
            } else if (device instanceof RadiantCraftingTableBlockEntity table) {
                for (ItemStack stack : table.items()) contents.add(stack, position, "Radiant Crafting Table");
            } else if (device instanceof GemCuttersTableBlockEntity table) {
                // Upstream exposes the eighteen input slots, not its separate output/recipe preview.
                for (int slot = 0; slot < 18; slot++) contents.add(table.getInput(slot), position, "Gem Cutter's Table");
            } else if (device instanceof MonitoringCrystalBlockEntity crystal) {
                contents.monitor(crystal);
            }
        }
        List<Entry> result = new ArrayList<>();
        for (var variants : contents.groups.values()) for (var group : variants)
            result.add(new Entry(group.stack, group.count, group.range, group.locations));
        return List.copyOf(result);
    }

    private void monitor(MonitoringCrystalBlockEntity crystal) {
        BlockEntity target = crystal.targetTile();
        if (target == null) return;
        var position = new BlockPosDimension(target.getBlockPos(), target.getLevel().dimension());
        if (monitored.contains(position)) return;
        BlockPosDimension partner = null;
        var state = target.getBlockState();
        if (target instanceof ChestBlockEntity && state.getBlock() instanceof ChestBlock
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            var adjacent = position.pos.relative(ChestBlock.getConnectedDirection(state));
            // The native combined handler may query its partner. Do not let that lookup load a missing chunk.
            if (!target.getLevel().hasChunkAt(adjacent)) return;
            var other = target.getLevel().getBlockState(adjacent);
            if (other.is(state.getBlock()) && other.getValue(ChestBlock.TYPE) == state.getValue(ChestBlock.TYPE).getOpposite()
                    && other.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING)
                    && target.getLevel().getBlockEntity(adjacent) instanceof ChestBlockEntity) {
                partner = new BlockPosDimension(adjacent, position.dimension);
                if (monitored.contains(partner)) return;
            }
        }
        var inventory = crystal.inventory();
        if (inventory == null || !monitoredHandlers.add(inventory)) return;
        monitored.add(position);
        if (partner != null) monitored.add(partner);
        //? if fabric {
        for (var view : inventory) {
            if (!view.isResourceBlank() && view.getAmount() > 0)
                add(view.getResource().toStack(), view.getAmount(), position, "Monitoring Crystal");
        }
        //?} else {
        /*for (int slot = 0; slot < inventory.getSlots(); slot++)
            add(inventory.getStackInSlot(slot), position, "Monitoring Crystal");
        *///?}
    }

    private void add(ItemStack stack, BlockPosDimension position, String description) {
        add(stack, stack.getCount(), position, description);
    }

    private void add(ItemStack stack, long amount, BlockPosDimension position, String description) {
        if (stack.isEmpty() || amount <= 0) return;
        Range range = !position.dimension.equals(viewer.level().dimension()) ? Range.OTHER_DIMENSION
            : position.pos.distSqr(viewer.blockPosition()) < rangeSquared ? Range.IN_RANGE : Range.OUT_OF_RANGE;
        var variants = groups.computeIfAbsent(stack.getItem(), ignored -> new ArrayList<>());
        Group selected = null;
        for (Group group : variants) {
            if (group.range == range && ExtendedItemStackHandler.sameItemAndData(group.stack, stack)) {
                selected = group;
                break;
            }
        }
        if (selected == null) {
            selected = new Group(stack, range);
            variants.add(selected);
        }
        selected.count = Math.addExact(selected.count, amount);
        // Upstream preserves separate descriptors for individual nonstackable items.
        if (stack.getMaxStackSize() == 1) {
            for (long index = 0; index < amount; index++) selected.locations.add(new Location(position, description, 1));
        } else selected.locations.add(new Location(position, description, amount));
    }

    private static final class Group {
        private final ItemStack stack;
        private final Range range;
        private final List<Location> locations = new ArrayList<>();
        private long count;
        private Group(ItemStack stack, Range range) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.range = range;
        }
    }
}
