package com.aranaira.arcanearchives.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/** Original through-wall locator rays, consuming only server-confirmed coordinates. */
public final class ManifestRayRenderer {
    private ManifestRayRenderer() {}

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
        if (client.player == null || client.level == null) return;
        var player = client.player.getPosition(partialTick);
        var beams = ManifestRays.beams(ManifestClient.tracking(), client.level.dimension(), player);
        var geometry = beams.stream().map(ManifestRays::corners).filter(corners -> corners.length != 0).toList();
        if (geometry.isEmpty()) return;
        int color = ManifestHighlight.color(client.level.getGameTime());
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        var shader = RenderSystem.getShader();
        float[] shaderColor = RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            //? if >=1.21 {
            var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            //?} else {
            /*var buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            *///?}
            for (var corners : geometry) for (int index : FACES) {
                Vec3 vertex = corners[index].subtract(camera.getPosition());
                //? if >=1.21 {
                buffer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z).setColor(color);
                //?} else {
                /*buffer.vertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z).color(color).endVertex();
                *///?}
            }
            //? if >=1.21 {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            //?} else {
            /*BufferUploader.drawWithShader(buffer.end());
            *///?}
        } finally {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            RenderSystem.depthMask(depthMask);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        }
    }
    private static final int[] FACES = {0, 1, 2, 3, 7, 6, 5, 4,
        0, 4, 5, 1, 1, 5, 6, 2, 2, 6, 7, 3, 3, 7, 4, 0};
}
