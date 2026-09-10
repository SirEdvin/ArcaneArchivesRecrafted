package com.aranaira.arcanearchives.mixin;

//? if !forge {
import com.aranaira.arcanearchives.items.SlaughtergleamItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//? if >=1.21 {
@Mixin(net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction.class)
//?} else {
/*@Mixin(net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction.class)
*///?}
public abstract class SlaughtergleamCountMixin {
    //? if >=1.21 {
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"), require = 1, allow = 1)
    private int arcanearchives$looting(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment, LivingEntity attacker) {
        int base = EnchantmentHelper.getEnchantmentLevel(enchantment, attacker);
        return base + (enchantment.is(net.minecraft.world.item.enchantment.Enchantments.LOOTING)
            ? SlaughtergleamItem.lootingBonus(attacker) : 0);
    }
    //?} else {
    /*@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getMobLooting(Lnet/minecraft/world/entity/LivingEntity;)I"), require = 1, allow = 1)
    private int arcanearchives$looting(LivingEntity attacker) {
        return EnchantmentHelper.getMobLooting(attacker) + SlaughtergleamItem.lootingBonus(attacker);
    }
    *///?}
}
//?}
