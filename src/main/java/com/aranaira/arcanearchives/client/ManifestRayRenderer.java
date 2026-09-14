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
        var positions = ManifestRays.positions(ManifestClient.tracking(), client.level.dimension());
        if (positions.isEmpty()) return;
        var player = client.player.getPosition(partialTick);
        var origin = player.add(0, 1, 0).subtract(camera.getPosition());
        int color = ManifestHighlight.color(client.level.getGameTime());
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        float lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        var shader = RenderSystem.getShader();
        float[] shaderColor = RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            for (var position : positions) {
                var corner = new Vec3(position.getX(), position.getY(), position.getZ());
                var target = corner.add(.5, .5, .5).subtract(camera.getPosition());
                RenderSystem.lineWidth(ManifestRays.width(player.distanceTo(corner)));
                //? if >=1.21 {
                var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                buffer.addVertex(matrix, (float) target.x, (float) target.y, (float) target.z).setColor(color);
                buffer.addVertex(matrix, (float) origin.x, (float) origin.y, (float) origin.z).setColor(color);
                BufferUploader.drawWithShader(buffer.buildOrThrow());
                //?} else {
                /*var buffer = Tesselator.getInstance().getBuilder();
                buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(matrix, (float) target.x, (float) target.y, (float) target.z).color(color).endVertex();
                buffer.vertex(matrix, (float) origin.x, (float) origin.y, (float) origin.z).color(color).endVertex();
                BufferUploader.drawWithShader(buffer.end());
                *///?}
            }
        } finally {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
            RenderSystem.lineWidth(lineWidth);
            RenderSystem.depthMask(depthMask);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        }
    }
}
