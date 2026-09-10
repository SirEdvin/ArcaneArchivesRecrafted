package com.aranaira.arcanearchives.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SwitchgleamItem extends ArcaneGemItem {
    public SwitchgleamItem() { super("switchgleam", 30, 150); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || gem.getItem() != this)
            return InteractionResultHolder.success(gem);
        Vec3 start = Vec3.atLowerCornerOf(player.blockPosition()).add(0, player.getEyeHeight(), 0);
        Vec3 end = start.add(player.getViewVector(0).scale(40));
        // Upstream uses encounter order, without nearest-hit sorting or block occlusion.
        for (var entity : level.getEntities(player, new AABB(start, end), EntitySelector.NO_SPECTATORS)) {
            if (entity.getBoundingBox().inflate(entity.getPickRadius()).clip(start, end).isEmpty()) continue;
            if (entity instanceof LivingEntity target && target.isAlive() && !target.isPassenger() && !target.isVehicle()
                    && !player.isPassenger() && !player.isVehicle()) {
                Vec3 playerPos = player.position();
                Vec3 targetPos = target.position();
                if (level.mayInteract(player, target.blockPosition()) && level.mayInteract(player, player.blockPosition())
                        && fits(server, player, targetPos) && fits(server, target, playerPos)) {
                    move(target, playerPos);
                    move(player, targetPos);
                }
            }
            break;
        }
        return InteractionResultHolder.success(gem);
    }

    private static boolean fits(ServerLevel level, LivingEntity entity, Vec3 destination) {
        AABB box = entity.getBoundingBox().move(destination.subtract(entity.position()));
        if (box.minY < level.getMinBuildHeight() || box.maxY > level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(box)) return false;
        // Check all touched chunks before consulting collisions; never load a destination to validate it.
        for (int x = net.minecraft.util.Mth.floor(box.minX) >> 4; x <= net.minecraft.util.Mth.floor(box.maxX) >> 4; x++) {
            for (int z = net.minecraft.util.Mth.floor(box.minZ) >> 4; z <= net.minecraft.util.Mth.floor(box.maxZ) >> 4; z++) {
                if (!level.hasChunk(x, z)) return false;
            }
        }
        // The other participant is leaving this position: check blocks, not its current entity collision.
        return !level.getBlockCollisions(entity, box).iterator().hasNext();
    }

    private static void move(LivingEntity entity, Vec3 pos) {
        float yaw = entity.getYRot() + (float) Math.PI;
        if (entity instanceof ServerPlayer player) player.connection.teleport(pos.x, pos.y, pos.z, yaw, entity.getXRot());
        else entity.moveTo(pos.x, pos.y, pos.z, yaw, entity.getXRot());
    }
}
