package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/** Per-viewer distance for the sender's current Manifest; never a storage permission. */
public record ManifestRequest(int containerId, int distance)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public ManifestRequest {
        if (containerId < 0 || distance < 0) throw new IllegalArgumentException("Invalid Manifest request");
    }
    public static final ResourceLocation ID = ContentRegistry.id("manifest_request");
    public static ManifestRequest read(FriendlyByteBuf buffer) {
        return new ManifestRequest(buffer.readVarInt(), buffer.readVarInt());
    }
    public static void write(FriendlyByteBuf buffer, ManifestRequest message) {
        buffer.writeVarInt(message.containerId());
        buffer.writeVarInt(message.distance());
    }
    private void apply(Player player) {
        if (player != null && player.containerMenu instanceof ManifestMenu menu && menu.containerId == containerId)
            menu.request(player, distance);
    }
    //? if >=1.21 {
    public static final Type<ManifestRequest> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ManifestRequest> CODEC = StreamCodec.of(ManifestRequest::write, ManifestRequest::read);
    @Override public Type<ManifestRequest> type() { return TYPE; }
    //?}
    //? if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "1", "1"::equals, "1"::equals);
    *///?}
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TYPE,
            (payload, context) -> context.server().execute(() -> payload.apply(context.player())));
        //?} else if fabric {
        /*net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(ID,
            (server, player, handler, buffer, sender) -> {
                ManifestRequest message = read(buffer);
                if (buffer.readableBytes() == 0) server.execute(() -> message.apply(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, ManifestRequest.class, (message, buffer) -> write(buffer, message), ManifestRequest::read,
            (message, supplier) -> {
                var context = supplier.get();
                context.enqueueWork(() -> message.apply(context.getSender()));
                context.setPacketHandled(true);
            }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
        *///?}
    }
    //? if neoforge {
    /*public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.apply(context.player())));
    }
    *///?}
    public static void send(int containerId, int distance) {
        ManifestRequest message = new ManifestRequest(containerId, distance);
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(message);
        //?} else if fabric {
        /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        write(buffer, message);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ID, buffer);
        *///?} else if forge {
        /*CHANNEL.sendToServer(message);
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToServer(message);
        *///?}
    }
}
