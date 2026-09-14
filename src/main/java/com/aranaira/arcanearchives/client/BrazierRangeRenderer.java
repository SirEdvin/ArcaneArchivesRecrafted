package com.aranaira.arcanearchives.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Solid-white translucent faces, equivalent to upstream's white-atlas range particle. */
public final class BrazierRangeRenderer {
    private BrazierRangeRenderer() {}
    private static final int[][] FACES = {{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            //? if >=1.21 {
            render(context.positionMatrix(), context.camera(), context.tickCounter().getGameTimeDeltaPartialTick(false));
            //?} else {
            /*render(context.matrixStack().last().pose(), context.camera(), context.tickDelta());
            *///?}
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.client.event.RenderLevelStageEvent event) -> {
                if (event.getStage() == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)
                    render(event.getPoseStack().last().pose(), event.getCamera(), event.getPartialTick());
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.client.event.RenderLevelStageEvent event) -> {
                if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)
                    render(event.getModelViewMatrix(), event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
            });
        *///?}
    }

    private static void render(Matrix4f matrix, Camera camera, float partialTick) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var shader = RenderSystem.getShader();
        float[] color = RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            BrazierRanges.INSTANCE.forEach(client.level, (device, state) -> {
                var bounds = device.rangeBounds(state.age(), partialTick).move(camera.getPosition().scale(-1));
                //? if >=1.21 {
                var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                //?} else {
                /*var buffer = Tesselator.getInstance().getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                *///?}
                for (var face : FACES) for (int corner : face) {
                    float x = (float) ((corner & 4) == 0 ? bounds.minX : bounds.maxX);
                    float y = (float) ((corner & 2) == 0 ? bounds.minY : bounds.maxY);
                    float z = (float) ((corner & 1) == 0 ? bounds.minZ : bounds.maxZ);
                    //? if >=1.21 {
                    buffer.addVertex(matrix, x, y, z).setColor(.78F, .54F, .19F, .4F);
                    //?} else {
                    /*buffer.vertex(matrix, x, y, z).color(.78F, .54F, .19F, .4F).endVertex();
                    *///?}
                }
                //? if >=1.21 {
                BufferUploader.drawWithShader(buffer.buildOrThrow());
                //?} else {
                /*BufferUploader.drawWithShader(buffer.end());
                *///?}
            });
        } finally {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.depthMask(depthMask);
            RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        }
    }
}
