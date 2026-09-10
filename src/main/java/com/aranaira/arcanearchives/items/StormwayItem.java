package com.aranaira.arcanearchives.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class StormwayItem extends ArcaneGemItem {
    public StormwayItem() { super("stormway", 30, 150); }
    @Override public boolean hasToggleMode() { return true; }

    private static boolean live(Player player) {
        return player.level() instanceof ServerLevel level && level.getServer().isSameThread()
            && player.isAlive() && !player.isSpectator();
    }
    private static void strike(ServerLevel level, double x, double y, double z) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(false);
            level.addFreshEntity(bolt);
        }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        if (!live(player) || charge(gem) == 0) return InteractionResultHolder.success(gem);
        var start = player.position().add(0, player.getBbHeight(), 0);
        var end = start.add(player.getLookAngle().scale(30));
        BlockPos first = BlockPos.containing(start), last = BlockPos.containing(end);
        for (int x = Math.min(first.getX() >> 4, last.getX() >> 4); x <= Math.max(first.getX() >> 4, last.getX() >> 4); x++)
            for (int z = Math.min(first.getZ() >> 4, last.getZ() >> 4); z <= Math.max(first.getZ() >> 4, last.getZ() >> 4); z++)
                if (!level.hasChunkAt(new BlockPos(x << 4, first.getY(), z << 4))) return InteractionResultHolder.fail(gem);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.success(gem);
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), gem))
            return InteractionResultHolder.fail(gem);
        if (level.canSeeSky(pos)) {
            strike((ServerLevel) level, pos.getX(), pos.getY(), pos.getZ());
            GemRecharge.consume(player, gem, 1);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        return InteractionResultHolder.success(gem);
    }
    public void tickAvailable(ItemStack stack, Player player) {
        Level level = player.level();
        if (!live(player) || !level.isRaining()
                || !AvailableGems.contains(player, stack)
                || charge(stack) >= maximumCharge(stack)) return;
        // Held gems recharge even under cover; only dropped gems require sky access.
        setCharge(stack, maximumCharge(stack));
        AvailableGems.changed(player);
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1F, 1F);
    }
    public static boolean rechargeInRain(ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !entity.isAlive() || !level.isRaining() || !level.canSeeSky(entity.blockPosition())) return false;
        ItemStack gem = entity.getItem();
        if (!(gem.getItem() instanceof StormwayItem) || charge(gem) >= maximumCharge(gem)) return false;
        ItemStack restored = gem.copy();
        setCharge(restored, maximumCharge(restored));
        entity.setItem(restored);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, 0.5F);
        return true;
    }
    //? if !fabric {
    /*@Override public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) { return rechargeInRain(entity); }
    *///?}

    public static void retaliate(LivingEntity victim, DamageSource damage) {
        if (!(victim instanceof Player player) || !live(player) || !(damage.getEntity() instanceof Mob attacker)
                || attacker.level() != player.level() || !damage.is(DamageTypeTags.IS_PROJECTILE)) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!level.hasChunkAt(attacker.blockPosition()) || !level.mayInteract(player, attacker.blockPosition())) return;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (!(gem.getItem() instanceof StormwayItem) || !isToggledOn(gem) || charge(gem) == 0) continue;
            strike(level, attacker.getX(), attacker.getY() + 0.5, attacker.getZ());
            attacker.addEffect(new MobEffectInstance(attacker.isInvertedHealAndHarm() ? MobEffects.HEAL : MobEffects.HARM, 1, 10));
            GemRecharge.consume(player, gem, 1);
            player.getInventory().setChanged();
        }
    }
    public static void struck(Entity entity) {
        if (!(entity instanceof Player player) || !live(player)) return;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (!(gem.getItem() instanceof StormwayItem) || charge(gem) == 0) continue;
            var tag = data(gem);
            long now = System.currentTimeMillis();
            if (tag.contains("cooldown") && tag.getLong("cooldown") + 1000 >= now) continue;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
            GemRecharge.consume(player, gem, 6);
            updateData(gem, current -> current.putLong("cooldown", now));
            AvailableGems.changed(player);
        }
    }
}
