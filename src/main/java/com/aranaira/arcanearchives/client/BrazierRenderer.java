package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;


/** Upstream BrazierTESR draws the standalone fire mesh without world lighting. */
public final class BrazierRenderer implements BlockEntityRenderer<BrazierBlockEntity> {
    public BrazierRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(BrazierBlockEntity brazier, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        var minecraft = Minecraft.getInstance();
        // Resolve against the current manager each frame; do not retain a model across resource reloads.
        //? if neoforge {
        /*var location = new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("arcanearchives", "block/brazier_of_hoarding_fire"),
            net.minecraft.client.resources.model.ModelResourceLocation.STANDALONE_VARIANT);
        *///?} else if >=1.21 {
        var location = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("arcanearchives", "block/brazier_of_hoarding_fire");
        //?} else {
        /*var location = new net.minecraft.resources.ResourceLocation("arcanearchives", "block/brazier_of_hoarding_fire");
        *///?}
        var model = minecraft.getModelManager().getModel(location);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(poses.last(),
            buffers.getBuffer(BrazierRenderType.FIRE),
            null, model, 1F, 1F, 1F, LightTexture.FULL_BRIGHT, overlay);
    }
}
