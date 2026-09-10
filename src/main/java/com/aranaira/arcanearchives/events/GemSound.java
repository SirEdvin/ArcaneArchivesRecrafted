package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/** Only the three implemented upstream GemParticle effects; no client requests. */
public record GemSound(Effect effect)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public enum Effect { MUNCHSTONE, MINDSPINDLE, PHOENIXWAY }
    public GemSound { java.util.Objects.requireNonNull(effect); }
    public static final ResourceLocation ID = ContentRegistry.id("gem_sound");
    public static GemSound read(FriendlyByteBuf buffer) { return new GemSound(buffer.readEnum(Effect.class)); }
    public static void write(FriendlyByteBuf buffer, GemSound message) { buffer.writeEnum(message.effect()); }
    //? if >=1.21 {
    public static final Type<GemSound> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, GemSound> CODEC = StreamCodec.of(GemSound::write, GemSound::read);
    @Override public Type<GemSound> type() { return TYPE; }
    //?}
    //? if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "1", "1"::equals, "1"::equals);
    *///?}
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
        //?} else if forge {
        /*CHANNEL.registerMessage(0, GemSound.class, (message, buffer) -> write(buffer, message), GemSound::read,
            (message, supplier) -> {
                var context = supplier.get();
                context.enqueueWork(() -> com.aranaira.arcanearchives.client.GemSoundClient.play(message));
                context.setPacketHandled(true);
            }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
        *///?}
    }
    //? if neoforge {
    /*public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC,
            (payload, context) -> context.enqueueWork(() -> com.aranaira.arcanearchives.client.GemSoundClient.play(payload)));
    }
    *///?}
    public static void send(Player player, Effect effect) {
        if (!(player.level() instanceof ServerLevel level) || !level.getServer().isSameThread()) return;
        var message = new GemSound(effect);
        //? if fabric {
        for (var recipient : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
            //? if >=1.21 {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(recipient, message);
            //?} else {
            /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            write(buffer, message);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(recipient, ID, buffer);
            *///?}
        }
        //?} else if forge {
        /*CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(player, message);
        *///?}
    }
}
