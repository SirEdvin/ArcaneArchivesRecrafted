package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.events.SwitchgleamEvents;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class SwitchgleamTeleportMixin {
    @Inject(method = "teleport(DDD)Z", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/EnderMan;randomTeleport(DDDZ)Z"), cancellable = true)
    private void arcanearchives$suppress(double x, double y, double z, CallbackInfoReturnable<Boolean> callback) {
        if (SwitchgleamEvents.suppress((EnderMan) (Object) this)) callback.setReturnValue(false);
    }
}
//?}
