package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;

/** One model per occupied ingredient slot, never a count-dependent dropped-item pile. */
public final class RadiantCraftingTableRenderer implements BlockEntityRenderer<RadiantCraftingTableBlockEntity> {
    private final ItemRenderer items;
    public RadiantCraftingTableRenderer(BlockEntityRendererProvider.Context context) { items = context.getItemRenderer(); }

    @Override public void render(RadiantCraftingTableBlockEntity table, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        for (int slot = 0; slot < 9; slot++) {
            var stack = table.items().get(slot);
            if (stack.isEmpty()) continue;
            poses.pushPose();
            try {
                poses.translate(.36 + slot % 3 * .14, .975, .36 + slot / 3 * .14);
                poses.mulPose(Axis.XP.rotationDegrees(90));
                poses.scale(.14F, .14F, .14F);
                items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poses, buffers, table.getLevel(), slot);
            } finally { poses.popPose(); }
        }
    }
}
