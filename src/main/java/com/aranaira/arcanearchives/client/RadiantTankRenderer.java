package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.inventory.RadiantTankStorage;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native fluid sprites/tint in the release Tank's internal fluid volume. */
public final class RadiantTankRenderer implements BlockEntityRenderer<RadiantTankBlockEntity> {
    public RadiantTankRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(RadiantTankBlockEntity tank, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        renderContents(tank, poses, buffers, light, overlay);
    }

    /** Draw only the packed fluid; the native item renderer owns shell and transforms. */
    public static void renderItem(net.minecraft.world.item.ItemStack stack, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        if (stack.isEmpty() || !(stack.getItem() instanceof com.aranaira.arcanearchives.items.RadiantTankItem item)) return;
        var level = Minecraft.getInstance().level;
        //? if >=1.21 {
        var component = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        if (component == null || level == null) return;
        var data = component.copyTag();
        //?} else {
        /*var tag = net.minecraft.world.item.BlockItem.getBlockEntityData(stack);
        if (tag == null) return;
        var data = tag.copy();
        *///?}
        var tank = new RadiantTankBlockEntity(net.minecraft.core.BlockPos.ZERO, item.getBlock().defaultBlockState());
        try {
            //? if >=1.21 {
            tank.loadWithComponents(data, level.registryAccess());
            //?} else {
            /*tank.load(data);
            *///?}
        } catch (IllegalArgumentException exception) {
            return; // Invalid packed data remains untouched; the item tooltip reports it.
        }
        renderContents(tank, poses, buffers, light, overlay);
    }

    private static void renderContents(RadiantTankBlockEntity tank, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        if (tank.inventory().storedAmount() == 0) return;
        TextureAtlasSprite top;
        TextureAtlasSprite side;
        int color;
        //? if fabric {
        var fluid = tank.inventory().variant;
        var sprites = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getSprites(fluid);
        if (sprites == null || sprites.length == 0 || sprites[0] == null) return;
        top = sprites[0];
        side = sprites.length > 1 && sprites[1] != null ? sprites[1] : top;
        color = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getColor(fluid, tank.getLevel(), tank.getBlockPos());
        //?} else {
        /*var fluid = tank.inventory().getFluid();
        //? if forge {
        var extension = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
        //?} else {
        var extension = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
        //?}
        var still = extension.getStillTexture(fluid);
        var flowing = extension.getFlowingTexture(fluid);
        if (still == null) return;
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        top = atlas.apply(still);
        side = flowing == null ? top : atlas.apply(flowing);
        color = extension.getTintColor(fluid);
        *///?}
        float high = Math.max(.0501F, (float) tank.inventory().storedAmount()
            / RadiantTankStorage.capacityFor(tank.upgrades().getUpgradesCount()) * .9F);
        float low = .05F, near = .08F, far = .76F;
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        poses.pushPose();
        poses.translate(.08, .05, .08);
        face(vertices, poses.last(), top, color, light, overlay, 0, -1, 0,
            new float[][]{{near,low,near},{far,low,near},{far,low,far},{near,low,far}});
        face(vertices, poses.last(), top, color, light, overlay, 0, 1, 0,
            new float[][]{{near,high,near},{near,high,far},{far,high,far},{far,high,near}});
        face(vertices, poses.last(), side, color, light, overlay, 0, 0, -1,
            new float[][]{{near,low,near},{near,high,near},{far,high,near},{far,low,near}});
        face(vertices, poses.last(), side, color, light, overlay, 0, 0, 1,
            new float[][]{{near,low,far},{far,low,far},{far,high,far},{near,high,far}});
        face(vertices, poses.last(), side, color, light, overlay, -1, 0, 0,
            new float[][]{{near,low,near},{near,low,far},{near,high,far},{near,high,near}});
        face(vertices, poses.last(), side, color, light, overlay, 1, 0, 0,
            new float[][]{{far,low,near},{far,high,near},{far,high,far},{far,low,far}});
        poses.popPose();
    }
    private static void face(VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite,
            int color, int light, int overlay, float nx, float ny, float nz, float[][] points) {
        for (int index = 0; index < 4; index++) {
            float[] p = points[index];
            float uCoord = ny != 0 ? p[0] : nz != 0 ? (nz < 0 ? .08F + .76F - p[0] : p[0])
                : nx > 0 ? .08F + .76F - p[2] : p[2];
            float high = Math.max(Math.max(points[0][1], points[1][1]), Math.max(points[2][1], points[3][1]));
            float vCoord = ny != 0 ? p[2] : p[1] == .05F ? high : .05F;
            float u = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uCoord;
            float v = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * vCoord;
            //? if >=1.21 {
            vertices.addVertex(pose.pose(), p[0], p[1], p[2]).setColor(color).setUv(u, v)
                .setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
            //?} else {
            /*vertices.vertex(pose.pose(), p[0], p[1], p[2]).color(color).uv(u, v)
                .overlayCoords(overlay).uv2(light).normal(nx, ny, nz).endVertex();
            *///?}
        }
    }
}
