package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/** Snapshot-bound tracking commands. No client positions, item data or ownership claims. */
public record ManifestSelect(int containerId, long revision, int index, int action)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public ManifestSelect {
        if (containerId < 0 || revision < 0 || action < 0 || action > 2 || index < -1 || action != 2 && index < 0) throw new IllegalArgumentException("Invalid Manifest request");
    }
    public static final ResourceLocation ID = ContentRegistry.id("manifest_select");
    public static ManifestSelect read(FriendlyByteBuf buffer) {
        return new ManifestSelect(buffer.readVarInt(), buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt());
    }
    public static void write(FriendlyByteBuf buffer, ManifestSelect message) {
        buffer.writeVarInt(message.containerId());
        buffer.writeVarLong(message.revision());
        buffer.writeVarInt(message.index());
        buffer.writeVarInt(message.action());
    }
    public boolean apply(Player player) {
        return com.aranaira.arcanearchives.data.ManifestTracking.apply(player, containerId, revision, index, action);
    }
    //? if >=1.21 {
    public static final Type<ManifestSelect> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ManifestSelect> CODEC = StreamCodec.of(ManifestSelect::write, ManifestSelect::read);
    @Override public Type<ManifestSelect> type() { return TYPE; }
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
                ManifestSelect message = read(buffer);
                if (buffer.readableBytes() == 0) server.execute(() -> message.apply(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, ManifestSelect.class, (message, buffer) -> write(buffer, message), ManifestSelect::read,
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
    public static void send(int containerId, long revision, int index, int action) {
        ManifestSelect message = new ManifestSelect(containerId, revision, index, action);
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
