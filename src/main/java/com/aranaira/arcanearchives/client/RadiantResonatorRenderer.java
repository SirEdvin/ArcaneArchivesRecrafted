package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Visual growth only: the server still creates the harvestable block on completion. */
public final class RadiantResonatorRenderer implements BlockEntityRenderer<RadiantResonatorBlockEntity> {
    private final BlockRenderDispatcher blocks;
    public RadiantResonatorRenderer(BlockEntityRendererProvider.Context context) { blocks = context.getBlockRenderDispatcher(); }

    @Override public void render(RadiantResonatorBlockEntity entity, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        float progress = entity.visualProgress();
        if (!(progress > 0) || entity.getLevel() == null || !entity.getLevel().isEmptyBlock(entity.getBlockPos().above())) return;
        progress = Math.min(1F, progress);
        poses.pushPose();
        try {
            poses.translate(.5, 1, .5);
            poses.scale(progress, progress, progress);
            poses.translate(-.5, 0, -.5);
            blocks.renderSingleBlock(ContentRegistry.RAW_QUARTZ_CLUSTER.get().defaultBlockState(), poses, buffers, light, overlay);
        } finally { poses.popPose(); }
    }
    @Override public boolean shouldRenderOffScreen(RadiantResonatorBlockEntity entity) { return true; }
}
