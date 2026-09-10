package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Release RadiantChestTESR's horizontal display geometry and FIXED item transform. */
public final class RadiantChestRenderer implements BlockEntityRenderer<RadiantChestBlockEntity> {
    private final ItemRenderer items;
    public RadiantChestRenderer(BlockEntityRendererProvider.Context context) { items = context.getItemRenderer(); }

    @Override public void render(RadiantChestBlockEntity chest, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        var facing = chest.displayFacing();
        if (facing.getAxis().isVertical()) return; // Up/down placement is stored but not drawn upstream.
        ItemStack stack = chest.displayStack();
        if (stack.isEmpty()) return;
        float angle = switch (facing) {
            case SOUTH -> 180;
            case EAST -> 270;
            case WEST -> 90;
            default -> 0;
        };
        poses.pushPose();
        try {
            poses.translate(.5 + facing.getStepX() * .47, .435, .5 + facing.getStepZ() * .47);
            poses.mulPose(Axis.YP.rotationDegrees(angle));
            poses.scale(.6F, .6F, .6F);
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poses, buffers, chest.getLevel(), 0);
        } finally {
            poses.popPose();
        }
    }
}
