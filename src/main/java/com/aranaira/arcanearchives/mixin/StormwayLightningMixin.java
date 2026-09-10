package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.StormwayItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightningBolt.class)
public abstract class StormwayLightningMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"))
    private void arcanearchives$stormwayStrike(Entity entity, ServerLevel level, LightningBolt bolt) {
        StormwayItem.struck(entity);
        entity.thunderHit(level, bolt);
    }
}
//?}
