package com.aranaira.arcanearchives.mixin;

//? if !forge {
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public abstract class SlaughtergleamEquipmentMixin {
    // 1.20 already passes the augmented integer level to custom death loot.
    //? if >=1.21 {
    @org.spongepowered.asm.mixin.injection.Redirect(method = "dropCustomDeathLoot", at = @org.spongepowered.asm.mixin.injection.At(
        value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processEquipmentDropChance(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F"), require = 1, allow = 1)
    private float arcanearchives$equipmentLooting(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float chance) {
        float nativeChance = net.minecraft.world.item.enchantment.EnchantmentHelper.processEquipmentDropChance(level, entity, source, chance);
        return nativeChance + 0.01F * com.aranaira.arcanearchives.items.SlaughtergleamItem.lootingBonus(source.getEntity());
    }
    //?}
}
//?}
