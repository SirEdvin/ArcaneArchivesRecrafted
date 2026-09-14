package com.aranaira.arcanearchives.mixin;

import com.aranaira.arcanearchives.client.RadiantTankRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Add the original packed-fluid pass after shell drawing, before its transform is popped. */
@Mixin(ItemRenderer.class)
public abstract class TankItemRenderMixin {
    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            //? if >=1.21 {
            ordinal = 0
            //?} else {
            /*ordinal = 1
            *///?}
        ), require = 1, allow = 1)
    private void arcanearchives$renderTankFluid(ItemStack stack, ItemDisplayContext context, boolean leftHand,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        RadiantTankRenderer.renderItem(stack, poses, buffers, light, overlay);
        com.aranaira.arcanearchives.client.AmphoraRenderer.renderItem(stack, poses, buffers, light, overlay);
    }
}
