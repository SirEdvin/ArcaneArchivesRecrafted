package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.events.GemSound;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

/** Upstream intentionally plays at each recipient, not at the remote gem. */
public final class GemSoundClient {
    private GemSoundClient() {}
    public static void play(GemSound message) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var sound = switch (message.effect()) {
            case MUNCHSTONE -> SoundEvents.PLAYER_BURP;
            case MINDSPINDLE -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case PHOENIXWAY -> SoundEvents.FIRECHARGE_USE;
        };
        float pitch = message.effect() == GemSound.Effect.MUNCHSTONE ? player.getRandom().nextFloat() * .5F + .75F : 1F;
        player.playSound(sound, 1F, pitch);
    }
    //? if fabric {
    public static void initialize() {
        //? if >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(GemSound.TYPE,
            (message, context) -> context.client().execute(() -> play(message)));
        //?} else {
        /*net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(GemSound.ID,
            (client, handler, buffer, sender) -> {
                GemSound message = GemSound.read(buffer);
                if (buffer.readableBytes() == 0) client.execute(() -> play(message));
            });
        *///?}
    }
    //?}
}
