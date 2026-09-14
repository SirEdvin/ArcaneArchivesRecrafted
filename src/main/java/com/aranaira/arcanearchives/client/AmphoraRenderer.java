package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Read-only client presentation; never consult the server-backed Amphora transfer adapter. */
public final class AmphoraRenderer {
    private AmphoraRenderer() {}
    public static void renderItem(ItemStack stack, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (!stack.is(ContentRegistry.RADIANT_AMPHORA.get()) || !RadiantAmphoraItem.linked(stack)) return;
        var client = Minecraft.getInstance();
        var level = client.level;
        var data = RadiantAmphoraItem.data(stack);
        var position = BlockPos.of(data.getLong("homeTank"));
        RadiantTankBlockEntity tank = level != null && level.dimension().location().toString().equals(data.getString("homeTankDim"))
            && level.hasChunkAt(position) && level.getBlockEntity(position) instanceof RadiantTankBlockEntity found ? found : null;
        TextureAtlasSprite sprite;
        int color;
        // The original model declares water and returns that model when no fluid is available.
        //? if fabric {
        var fluid = tank != null && tank.inventory().storedAmount() > 0 ? tank.inventory().getResource()
            : net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant.of(net.minecraft.world.level.material.Fluids.WATER);
        var sprites = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getSprites(fluid);
        if (sprites == null || sprites.length == 0 || sprites[0] == null) return;
        sprite = sprites[0];
        color = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getColor(fluid);
        //?} else {
        /*var fluid = tank != null && tank.inventory().storedAmount() > 0 ? tank.inventory().getFluid()
            //? if forge {
            /^: new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        var extension = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
            ^///?} else {
            : new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        var extension = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
            //?}
        var still = extension.getStillTexture(fluid);
        if (still == null) return;
        sprite = client.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(still);
        color = extension.getTintColor(fluid);
        *///?}
        // Resolve the current atlas every draw so reloads cannot leave retained sprite references.
        var mask = client.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ContentRegistry.id(
            "item/radiant_amphora_fluidmask_" + (RadiantAmphoraItem.filling(stack) ? "fill" : "empty")));
        var contents = mask.contents();
        var vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        int width = contents.width(), height = contents.height();
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            if (contents.isTransparent(0, x, y)) continue;
            for (int side = 0; side < 2; side++) {
                float z = (side == 0 ? 7.498F : 8.502F) / 16F;
                for (int corner = 0; corner < 4; corner++) {
                    int index = side == 0 ? corner : 3 - corner;
                    float u = (x + (index == 1 || index == 2 ? 1F : 0F)) / width;
                    float v = (y + (index >= 2 ? 1F : 0F)) / height;
                    float textureU = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * u;
                    float textureV = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * v;
                    //? if >=1.21 {
                    vertices.addVertex(poses.last().pose(), u, 1 - v, z).setColor(color).setUv(textureU, textureV)
                        .setOverlay(overlay).setLight(light).setNormal(poses.last(), 0, 0, side == 0 ? -1 : 1);
                    //?} else {
                    /*vertices.vertex(poses.last().pose(), u, 1 - v, z).color(color).uv(textureU, textureV)
                        .overlayCoords(overlay).uv2(light).normal(poses.last().normal(), 0, 0, side == 0 ? -1 : 1).endVertex();
                    *///?}
                }
            }
        }
    }
}
