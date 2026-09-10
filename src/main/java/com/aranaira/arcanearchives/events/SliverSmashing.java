package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SliverSmashing {
    public record Outcome(int count, boolean consumesQuartz) {}

    static Outcome roll(ServerSideConfig settings, IntUnaryOperator random) {
        int roll = random.applyAsInt(100);
        if (roll <= settings.sliverClusterChance()) {
            int range = settings.sliverMaximum() - settings.sliverMinimum();
            return new Outcome(settings.sliverMinimum() + (range == 0 ? 0 : random.applyAsInt(range)), true);
        }
        return new Outcome(roll <= settings.sliverClusterChance() + settings.sliverSingleChance() ? 1 : 0, false);
    }

    public static InteractionResult attack(Player player, Level level, InteractionHand hand, BlockPos pos, Direction face) {
        if (level.isClientSide || hand != InteractionHand.MAIN_HAND || face == null
            || !player.isAlive() || player.isSpectator() || !player.getAbilities().mayBuild
            || !player.getMainHandItem().is(ContentRegistry.RAW_QUARTZ.get())
            || player.distanceToSqr(Vec3.atCenterOf(pos)) > 36
            || !level.getWorldBorder().isWithinBounds(pos) || !level.hasChunkAt(pos)
            || !level.mayInteract(player, pos) || level.getBlockState(pos).isAir()) return InteractionResult.PASS;
        ItemStack quartz = player.getMainHandItem();
        Outcome outcome = roll(ServerSideConfig.current(), level.random::nextInt);
        if (outcome.count() == 0) return InteractionResult.PASS;
        Vec3 hit = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        ItemEntity drop = new ItemEntity(level, hit.x, hit.y, hit.z, new ItemStack(ContentRegistry.QUARTZ_SLIVER_ITEM.get(), outcome.count()));
        drop.setDeltaMovement(level.random.nextFloat() * 0.4F - 0.2F, level.random.nextFloat() * 0.2F + 0.2F,
            level.random.nextFloat() * 0.4F - 0.2F);
        if (outcome.consumesQuartz()) quartz.shrink(1);
        if (!level.addFreshEntity(drop) && outcome.consumesQuartz()) quartz.grow(1);
        return InteractionResult.PASS;
    }

    public static void initialize() {
        //? if fabric {
        var phase = ContentRegistry.id("sliver_smashing");
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register(phase, SliverSmashing::attack);
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false, SliverSmashing::onAttack);
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, false, SliverSmashing::onAttack);
        *///?}
    }

    //? if forge {
    /*private static void onAttack(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != event.getAction().START || event.isCanceled()) return;
        if (event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.DENY
            || event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY) return;
        attack(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
    }
    *///?} else if neoforge {
    /*private static void onAttack(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != event.getAction().START || event.isCanceled()) return;
        if (event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE
            || event.getUseItem() == net.neoforged.neoforge.common.util.TriState.FALSE) return;
        attack(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
    }
    *///?}

    private SliverSmashing() {}
}
