package com.aranaira.arcanearchives.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/** Cutout entity state with an unlit vertex shader, not a translucent/emissive blend mode. */
public final class BrazierRenderType extends RenderType {
    private static ShaderInstance shader;
    public static final RenderType FIRE = create("arcanearchives_brazier_fire", DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS, 1536, true, false, CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> shader))
            .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
            .setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));

    private BrazierRenderType() {
        super("unused", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false, () -> {}, () -> {});
    }
    public static ResourceLocation id() {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath("arcanearchives", "brazier_fire");
        //?} else {
        /*return new ResourceLocation("arcanearchives", "brazier_fire");
        *///?}
    }
    public static void loaded(ShaderInstance replacement) { shader = replacement; }
}
