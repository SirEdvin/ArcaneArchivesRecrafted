package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.RivertearItem;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class RivertearRechargeMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void arcanearchives$rechargeRivertear(CallbackInfo callback) {
        com.aranaira.arcanearchives.items.MountaintearItem.rechargeInLava((ItemEntity) (Object) this);
        if (RivertearItem.rechargeInWater((ItemEntity) (Object) this)) callback.cancel();
        else if (com.aranaira.arcanearchives.items.StormwayItem.rechargeInRain((ItemEntity) (Object) this)) callback.cancel();
    }
}
//?}
