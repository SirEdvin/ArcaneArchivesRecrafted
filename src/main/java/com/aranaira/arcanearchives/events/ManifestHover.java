package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.data.ManifestTracking;
import com.aranaira.arcanearchives.init.ContentRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/** A bounded item reference, never client-supplied storage locations or grants. */
public record ManifestHover(int containerId, int distance, boolean keepOpen, byte[] reference)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public static final int MAX_REFERENCE_BYTES = 16384;
    public static final ResourceLocation ID = ContentRegistry.id("manifest_hover");
    public ManifestHover {
        if (containerId < 0 || distance < 0 || reference.length == 0 || reference.length > MAX_REFERENCE_BYTES)
            throw new IllegalArgumentException("Invalid Manifest hover request");
        reference = reference.clone();
    }
    @Override public byte[] reference() { return reference.clone(); }
    public static ManifestHover read(FriendlyByteBuf buffer) {
        return new ManifestHover(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(),
            buffer.readByteArray(MAX_REFERENCE_BYTES));
    }
    public static void write(FriendlyByteBuf buffer, ManifestHover message) {
        buffer.writeVarInt(message.containerId());
        buffer.writeVarInt(message.distance());
        buffer.writeBoolean(message.keepOpen());
        buffer.writeByteArray(message.reference);
    }
    public boolean apply(Player player) {
        return ManifestTracking.fromHover(player, containerId, distance, keepOpen, () -> {
            var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(reference));
            try {
                var tag = buffer.readNbt();
                if (tag == null || buffer.isReadable()) throw new IllegalArgumentException("Invalid hovered item");
                //? if >=1.21 {
                return ItemStack.parse(player.level().registryAccess(), tag).orElse(ItemStack.EMPTY);
                //?} else {
                /*return ItemStack.of(tag);
                *///?}
            } finally { buffer.release(); }
        });
    }
    public static byte[] encode(ItemStack reference, HolderLookup.Provider registries) {
        var stack = reference.copy();
        stack.setCount(1);
        var buffer = new FriendlyByteBuf(Unpooled.buffer(128, MAX_REFERENCE_BYTES));
        try {
            //? if >=1.21 {
            buffer.writeNbt(stack.save(registries));
            //?} else {
            /*buffer.writeNbt(stack.save(new net.minecraft.nbt.CompoundTag()));
            *///?}
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally { buffer.release(); }
    }
    //? if >=1.21 {
    public static final Type<ManifestHover> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ManifestHover> CODEC = StreamCodec.of(ManifestHover::write, ManifestHover::read);
    @Override public Type<ManifestHover> type() { return TYPE; }
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
                ManifestHover message = read(buffer);
                if (buffer.readableBytes() == 0) server.execute(() -> message.apply(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, ManifestHover.class, (message, buffer) -> write(buffer, message), ManifestHover::read,
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
    public static void send(Player player, ItemStack stack, int distance, boolean keepOpen) {
        ManifestHover message = new ManifestHover(player.containerMenu.containerId, distance, keepOpen,
            encode(stack, player.level().registryAccess()));
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
