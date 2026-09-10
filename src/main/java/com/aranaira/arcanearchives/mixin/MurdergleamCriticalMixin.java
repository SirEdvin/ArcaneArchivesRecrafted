package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.MurdergleamItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class MurdergleamCriticalMixin {
    // Final critical flag, after the sprint exclusion; verified in both mapped attack methods.
    //? if >=1.21 {
    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), index = 9, require = 1, allow = 1)
    //?} else {
    /*@ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 1), index = 8, require = 1, allow = 1)
    *///?}
    private boolean arcanearchives$murdergleamCritical(boolean vanillaCritical) {
        return MurdergleamItem.criticalHit((Player) (Object) this, vanillaCritical) || vanillaCritical;
    }
}
//?}
