package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.ElixirspindleItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ElixirspindleUseMixin {
    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void arcanearchives$start(InteractionHand hand, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.isUsingItem() && !entity.getItemInHand(hand).isEmpty() && ElixirspindleItem.intercept(entity)) callback.cancel();
    }
    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void arcanearchives$tick(ItemStack stack, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ElixirspindleItem.intercept(entity)) {
            entity.stopUsingItem();
            callback.cancel();
        }
    }
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void arcanearchives$stop(CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.getUseItem().isEmpty() && ElixirspindleItem.intercept(entity)) {
            entity.stopUsingItem();
            callback.cancel();
        }
    }
}
//?}
