package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.SlaughtergleamItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SlaughtergleamDeathMixin {
    // AFTER_DEATH runs after loot; payment must precede the loot-level queries.
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), require = 1)
    //? if >=1.21 {
    private void arcanearchives$paySlaughtergleam(net.minecraft.server.level.ServerLevel level, DamageSource source, CallbackInfo ci) {
    //?} else {
    /*private void arcanearchives$paySlaughtergleam(DamageSource source, CallbackInfo ci) {
    *///?}
        SlaughtergleamItem.onDeath((LivingEntity) (Object) this, source);
    }
    //? if <1.21 {
    /*@org.spongepowered.asm.mixin.injection.Redirect(method = "dropAllDeathLoot", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getMobLooting(Lnet/minecraft/world/entity/LivingEntity;)I"), require = 1, allow = 1)
    private int arcanearchives$customLooting(LivingEntity attacker) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getMobLooting(attacker)
            + SlaughtergleamItem.lootingBonus(attacker);
    }
    *///?}
}
//?}
