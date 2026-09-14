package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.data.ManifestTracking;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

public record ClearManifestTracking()
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public static final ResourceLocation ID = ContentRegistry.id("clear_manifest_tracking");
    //? if >=1.21 {
    public static final Type<ClearManifestTracking> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClearManifestTracking> CODEC = StreamCodec.unit(new ClearManifestTracking());
    @Override public Type<ClearManifestTracking> type() { return TYPE; }
    //?}
    //? if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "1", "1"::equals, "1"::equals);
    *///?}
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TYPE,
            (payload, context) -> context.server().execute(() -> ManifestTracking.clearFromKey(context.player())));
        //?} else if fabric {
        /*net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(ID,
            (server, player, handler, buffer, sender) -> {
                if (buffer.readableBytes() == 0) server.execute(() -> ManifestTracking.clearFromKey(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, ClearManifestTracking.class, (message, buffer) -> {}, buffer -> new ClearManifestTracking(),
            (message, supplier) -> {
                var context = supplier.get();
                context.enqueueWork(() -> { if (context.getSender() != null) ManifestTracking.clearFromKey(context.getSender()); });
                context.setPacketHandled(true);
            }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
        *///?}
    }
    //? if neoforge {
    /*public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC,
            (payload, context) -> context.enqueueWork(() -> ManifestTracking.clearFromKey(context.player())));
    }
    *///?}
    public static void send() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new ClearManifestTracking());
        //?} else if fabric {
        /*net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ID, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty());
        *///?} else if forge {
        /*CHANNEL.sendToServer(new ClearManifestTracking());
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ClearManifestTracking());
        *///?}
    }
}
