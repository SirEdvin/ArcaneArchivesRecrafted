package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

public record AmphoraToggle()
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public static final ResourceLocation ID = ContentRegistry.id("amphora_toggle");
    //? if >=1.21 {
    public static final Type<AmphoraToggle> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, AmphoraToggle> CODEC = StreamCodec.unit(new AmphoraToggle());
    @Override public Type<AmphoraToggle> type() { return TYPE; }
    //?}
    //? if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "1", "1"::equals, "1"::equals);
    *///?}
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TYPE,
            (payload, context) -> context.server().execute(() -> RadiantAmphoraItem.toggle(context.player())));
        //?} else if fabric {
        /*net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(ID,
            (server, player, handler, buffer, sender) -> {
                if (buffer.readableBytes() == 0) server.execute(() -> RadiantAmphoraItem.toggle(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, AmphoraToggle.class, (message, buffer) -> {}, buffer -> new AmphoraToggle(),
            (message, supplier) -> {
                var context = supplier.get();
                context.enqueueWork(() -> { if (context.getSender() != null) RadiantAmphoraItem.toggle(context.getSender()); });
                context.setPacketHandled(true);
            }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
        *///?}
    }
    //? if neoforge {
    /*public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC,
            (payload, context) -> context.enqueueWork(() -> RadiantAmphoraItem.toggle(context.player())));
    }
    *///?}
    public static void send() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new AmphoraToggle());
        //?} else if fabric {
        /*net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ID, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty());
        *///?} else if forge {
        /*CHANNEL.sendToServer(new AmphoraToggle());
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToServer(new AmphoraToggle());
        *///?}
    }
}
