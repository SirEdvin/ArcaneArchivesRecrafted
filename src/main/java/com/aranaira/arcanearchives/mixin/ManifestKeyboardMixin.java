package com.aranaira.arcanearchives.mixin;

import com.aranaira.arcanearchives.client.ManifestKey;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared pre-screen input hook, including recipe-viewer screens which are not native containers. */
@Mixin(KeyboardHandler.class)
public abstract class ManifestKeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void arcaneArchives$hoverKey(long window, int key, int scan, int action, int modifiers, CallbackInfo callback) {
        if (action == org.lwjgl.glfw.GLFW.GLFW_PRESS && window == Minecraft.getInstance().getWindow().getWindow()
                && ManifestKey.trackHovered(key, scan)) callback.cancel();
    }
}
