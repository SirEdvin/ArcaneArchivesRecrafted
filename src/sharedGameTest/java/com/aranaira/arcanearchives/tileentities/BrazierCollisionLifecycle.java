package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native inventories/entities; only spawn rejection is injected at the actual spawn boundary. */
public final class BrazierCollisionLifecycle {
    public static void run(BrazierBlockEntity brazier, RadiantChestBlockEntity chest) {
        var level = brazier.getLevel();
        var pos = brazier.getBlockPos();
        var soundProbe = new BrazierBlockEntity(pos, brazier.getBlockState());
        var defaults = com.aranaira.arcanearchives.config.ServerSideConfig.DEFAULTS;
        require(soundProbe.pickupSoundDue(1000, defaults), "Initial absorption sound missing");
        require(!soundProbe.pickupSoundDue(1299, defaults), "Absorption sound bypassed 300-ms throttle");
        require(soundProbe.pickupSoundDue(1300, defaults), "Absorption sound rejected exact throttle boundary");
        for (String gate : new String[]{"UseSounds", "BrazierPickup"}) {
            var muted = defaults.toProperties();
            muted.setProperty(gate, "false");
            require(!soundProbe.pickupSoundDue(2000, com.aranaira.arcanearchives.config.ServerSideConfig.fromProperties(muted)),
                "Absorption sound ignored " + gate);
        }
        require(soundProbe.pickupSoundDue(2000, defaults), "Muted sound consumed the throttle window");
        require(!soundProbe.pickupSoundDue(1900, defaults), "Backwards clock bypassed sound throttle");
        var inventory = chest.inventory();
        var previous = new ArrayList<ItemStack>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) previous.add(inventory.getStackInSlot(slot).copy());
        var entities = new ArrayList<ItemEntity>();
        try {
            for (int slot = 0; slot < inventory.getSlots(); slot++)
                inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, inventory.getStackLimit(slot, new ItemStack(Items.COBBLESTONE))));
            int batchCapacity = inventory.getStackLimit(0, new ItemStack(Items.DIAMOND));
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, batchCapacity - 5));
            var inputs = java.util.List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.DIAMOND, 7), new ItemStack(Items.DIAMOND, 2));
            var remainders = brazier.insertBatch(inputs.get(0), inputs);
            require(remainders.size() == 2 && remainders.get(0).getCount() == 7 - (5 - 3)
                && remainders.get(1).getCount() == 2 && inventory.getStackInSlot(0).getCount() == batchCapacity,
                "Batch routing lost partial remainder or later input order");
            require(inputs.get(0).getCount() == 3 && inputs.get(1).getCount() == 7 && inputs.get(2).getCount() == 2,
                "Batch routing mutated offered stacks");
            remainders.get(0).setCount(1);
            require(inputs.get(1).getCount() == 7 && inventory.getStackInSlot(0).getCount() == batchCapacity,
                "Batch remainder aliased source or destination");
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            require(brazier.insertBatch(inputs.get(0), inputs).isEmpty()
                && inventory.getStackInSlot(0).getCount() == 3 + 7 + 2, "Batch full acceptance failed conservation");
            for (int room : new int[]{7, 2, 0}) for (boolean failSpawn : new boolean[]{false, true}) {
                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, inventory.getStackLimit(slot, new ItemStack(Items.COBBLESTONE))));
                int capacity = inventory.getStackLimit(0, new ItemStack(Items.DIAMOND));
                inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - room));
                var source = new ItemEntity(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, new ItemStack(Items.DIAMOND, 7));
                entities.add(source);
                if (room == 7 || !failSpawn) {
                    source.setPos(pos.getX() + .5, pos.getY() + 1.5, pos.getZ() + .5);
                    source.setDeltaMovement(0, 0, 0);
                }
                require(level.addFreshEntity(source), "Could not install collision source");
                var ejected = new ArrayList<ItemEntity>();
                if (room == 7 || !failSpawn) {
                    var area = new net.minecraft.world.phys.AABB(pos).inflate(1);
                    var before = level.getEntitiesOfClass(ItemEntity.class, area);
                    for (int tick = 0; tick < 40 && source.isAlive(); tick++) source.tick();
                    for (var entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
                        if (entity != source && !before.contains(entity)) {
                            ejected.add(entity);
                            entities.add(entity);
                        }
                    }
                } else {
                    brazier.absorb(source, rejected -> {
                        ejected.add(rejected);
                        entities.add(rejected);
                        return !failSpawn && level.addFreshEntity(rejected);
                    });
                }
                require(inventory.getStackInSlot(0).getCount() == capacity, "Collision did not accept exact available capacity");
                if (room == 7) {
                    require(!source.isAlive() && ejected.isEmpty(), "Full collision did not consume source");
                    brazier.absorb(source);
                    require(inventory.getStackInSlot(0).getCount() == capacity, "Dead source deposited twice");
                } else {
                    require(ejected.size() == 1, "Remainder spawned more than once");
                    var retained = failSpawn ? source : ejected.get(0);
                    require(source.isAlive() == failSpawn && retained.isAlive()
                        && retained.getItem().is(Items.DIAMOND) && retained.getItem().getCount() == 7 - room,
                        "Spawn result lost remainder or restored accepted items");
                    var saved = retained.saveWithoutId(new CompoundTag());
                    require(saved.getShort("PickupDelay") == 20, "Rejected pickup delay changed");
                    inventory.setStackInSlot(0, ItemStack.EMPTY);
                    retained.setPos(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
                    retained.setDeltaMovement(0, 0, 0);
                    retained.tick();
                    require(retained.isAlive() && retained.getItem().getCount() == 7 - room
                        && inventory.getStackInSlot(0).isEmpty(), "Native item tick routed a rejected remainder");
                    var replica = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.DIAMOND));
                    entities.add(replica);
                    replica.load(saved);
                    brazier.absorb(replica);
                    require(replica.isAlive() && inventory.getStackInSlot(0).isEmpty(), "Rejected marker failed native serialization");
                }
                entities.forEach(ItemEntity::discard);
                entities.clear();
            }
        } finally {
            entities.forEach(ItemEntity::discard);
            for (int slot = 0; slot < previous.size(); slot++) inventory.setStackInSlot(slot, previous.get(slot));
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
