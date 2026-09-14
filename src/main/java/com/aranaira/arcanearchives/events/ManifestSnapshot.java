package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/** Bounded S2C fragments; a menu publishes a decoded snapshot only after the final fragment. */
public record ManifestSnapshot(int containerId, long revision, int offset, int totalBytes, byte[] data)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public static final int CHUNK_BYTES = 24 * 1024;
    // Native opened menus use IDs 1–100; zero is reserved for connection-scoped tracking.
    public static final int TRACKING_ID = 0;
    public static final int MAX_BYTES = 16 * 1024 * 1024;
    public static final ResourceLocation ID = ContentRegistry.id("manifest_snapshot");

    public ManifestSnapshot {
        if (containerId < 0 || revision < 0 || totalBytes < 0 || totalBytes > MAX_BYTES || offset < 0
                || data.length > CHUNK_BYTES || offset > totalBytes - data.length
                || totalBytes != 0 && data.length == 0)
            throw new IllegalArgumentException("Invalid Manifest fragment bounds");
        data = data.clone();
    }
    @Override public byte[] data() { return data.clone(); }
    public static ManifestSnapshot read(FriendlyByteBuf buffer) {
        return new ManifestSnapshot(buffer.readVarInt(), buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray(CHUNK_BYTES));
    }
    public static void write(FriendlyByteBuf buffer, ManifestSnapshot message) {
        buffer.writeVarInt(message.containerId).writeVarLong(message.revision).writeVarInt(message.offset).writeVarInt(message.totalBytes).writeByteArray(message.data);
    }
    //? if >=1.21 {
    public static final Type<ManifestSnapshot> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ManifestSnapshot> CODEC = StreamCodec.of(ManifestSnapshot::write, ManifestSnapshot::read);
    @Override public Type<ManifestSnapshot> type() { return TYPE; }
    //?}
    //? if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "3", "3"::equals, "3"::equals);
    *///?}
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
        //?} else if forge {
        /*CHANNEL.registerMessage(0, ManifestSnapshot.class, (message, buffer) -> write(buffer, message), ManifestSnapshot::read,
            (message, supplier) -> {
                var context = supplier.get();
                var source = context.getNetworkManager();
                context.enqueueWork(() -> com.aranaira.arcanearchives.client.ManifestClient.receive(message, source));
                context.setPacketHandled(true);
            }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
        *///?}
    }
    //? if neoforge {
    /*public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToClient(TYPE, CODEC,
            (payload, context) -> {
                var source = context.connection();
                context.enqueueWork(() -> com.aranaira.arcanearchives.client.ManifestClient.receive(payload, source));
            });
    }
    *///?}
    public static void send(ServerPlayer player, int containerId, long revision, byte[] snapshot) {
        if (!player.server.isSameThread() || player.containerMenu.containerId != containerId) return;
        sendFragments(player, containerId, revision, snapshot);
    }
    public static void sendTracking(ServerPlayer player, long revision, byte[] snapshot) {
        if (!player.server.isSameThread() || player.server.getPlayerList().getPlayer(player.getUUID()) != player) return;
        sendFragments(player, TRACKING_ID, revision, snapshot);
    }
    private static void sendFragments(ServerPlayer player, int containerId, long revision, byte[] snapshot) {
        if (snapshot.length == 0) send(player, new ManifestSnapshot(containerId, revision, 0, 0, snapshot));
        for (int offset = 0; offset < snapshot.length; offset += CHUNK_BYTES)
            send(player, new ManifestSnapshot(containerId, revision, offset, snapshot.length,
                Arrays.copyOfRange(snapshot, offset, Math.min(snapshot.length, offset + CHUNK_BYTES))));
    }
    private static void send(ServerPlayer player, ManifestSnapshot message) {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, message);
        //?} else if fabric {
        /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        write(buffer, message);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, ID, buffer);
        *///?} else if forge {
        /*CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), message);
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, message);
        *///?}
    }

    public static byte[] encode(List<ManifestContents.Entry> entries, HolderLookup.Provider registries) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer(256, MAX_BYTES));
        try {
            buffer.writeVarInt(entries.size());
            for (var entry : entries) {
                //? if >=1.21 {
                buffer.writeNbt(entry.stack().save(registries));
                //?} else {
                /*buffer.writeNbt(entry.stack().save(new net.minecraft.nbt.CompoundTag()));
                *///?}
                buffer.writeLong(entry.count());
                buffer.writeEnum(entry.range());
                buffer.writeVarInt(entry.locations().size());
                for (var location : entry.locations()) {
                    buffer.writeNbt(location.position().serializeNBT());
                    buffer.writeUtf(location.description());
                    buffer.writeLong(location.count());
                }
            }
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally { buffer.release(); }
    }

    public static List<ManifestContents.Entry> decode(byte[] bytes, HolderLookup.Provider registries) {
        if (bytes.length == 0 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("Invalid Manifest snapshot size");
        var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            int size = boundedCount(buffer);
            List<ManifestContents.Entry> entries = new ArrayList<>();
            for (int index = 0; index < size; index++) {
                var tag = buffer.readNbt();
                if (tag == null) throw new IllegalArgumentException("Missing Manifest icon");
                //? if >=1.21 {
                ItemStack stack = ItemStack.parse(registries, tag).orElse(ItemStack.EMPTY);
                //?} else {
                /*ItemStack stack = ItemStack.of(tag);
                *///?}
                long count = buffer.readLong();
                var range = buffer.readEnum(ManifestContents.Range.class);
                int sources = boundedCount(buffer);
                if (stack.isEmpty() || stack.getCount() != 1 || count <= 0 || sources == 0)
                    throw new IllegalArgumentException("Invalid Manifest entry");
                List<ManifestContents.Location> locations = new ArrayList<>();
                long total = 0;
                for (int source = 0; source < sources; source++) {
                    var position = buffer.readNbt();
                    if (position == null) throw new IllegalArgumentException("Missing Manifest source");
                    var location = new ManifestContents.Location(BlockPosDimension.deserializeNBT(position), buffer.readUtf(), buffer.readLong());
                    if (location.count() <= 0) throw new IllegalArgumentException("Invalid Manifest source count");
                    total = Math.addExact(total, location.count());
                    locations.add(location);
                }
                if (total != count) throw new IllegalArgumentException("Manifest source total mismatch");
                entries.add(new ManifestContents.Entry(stack, count, range, locations));
            }
            if (buffer.isReadable()) throw new IllegalArgumentException("Trailing Manifest snapshot data");
            return List.copyOf(entries);
        } finally { buffer.release(); }
    }

    private static int boundedCount(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > buffer.readableBytes()) throw new IllegalArgumentException("Invalid Manifest list length");
        return count;
    }
}
