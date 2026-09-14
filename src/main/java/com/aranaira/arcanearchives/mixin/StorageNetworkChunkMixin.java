package com.aranaira.arcanearchives.mixin;

import com.aranaira.arcanearchives.data.StorageNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Index only native chunk-owned entities, not detached copies used by item/serialization previews. */
@Mixin(LevelChunk.class)
public abstract class StorageNetworkChunkMixin {
    @Inject(method = "setBlockEntity", at = @At("RETURN"))
    private void arcanearchives$installed(BlockEntity entity, CallbackInfo callback) {
        StorageNetworks.refresh((LevelChunk) (Object) this, entity.getBlockPos());
    }

    @Inject(method = "removeBlockEntity", at = @At("RETURN"))
    private void arcanearchives$removed(BlockPos pos, CallbackInfo callback) {
        StorageNetworks.refresh((LevelChunk) (Object) this, pos);
    }

    @Inject(method = "registerAllBlockEntitiesAfterLevelLoad", at = @At("RETURN"))
    private void arcanearchives$loaded(CallbackInfo callback) {
        StorageNetworks.loaded((LevelChunk) (Object) this);
    }

    @Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
    private void arcanearchives$unloading(CallbackInfo callback) {
        StorageNetworks.unloading((LevelChunk) (Object) this);
    }
}
