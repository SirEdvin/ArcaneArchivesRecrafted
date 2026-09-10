package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Fabric has no block-level removal hook carrying the breaking player. */
@Mixin(ServerPlayerGameMode.class)
public abstract class TroveRemovalMixin {
    @Shadow @Final protected ServerPlayer player;

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean arcanearchives$removeTrove(ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove)
            return trove.removeByPlayer(player, () -> level.removeBlock(pos, moving));
        return level.removeBlock(pos, moving);
    }
}
//?}
