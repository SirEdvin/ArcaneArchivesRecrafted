package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
//? if >=1.21 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
//?}

/** Two connection-scoped preferences; never carries a player identity or storage mutation. */
public record PlayerPreferences(boolean trovesDispense, boolean defaultRoutingNoNewItems)
    //? if >=1.21 {
    implements CustomPacketPayload
    //?}
{
    public static final ResourceLocation ID = ContentRegistry.id("player_preferences");
    private static final PlayerPreferences DEFAULTS = new PlayerPreferences(true, false);
    private static final Map<ServerPlayer, PlayerPreferences> PLAYERS = new WeakHashMap<>();
    //? if >=1.21 {
    public static final Type<PlayerPreferences> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, PlayerPreferences> CODEC = StreamCodec.ofMember(PlayerPreferences::write, PlayerPreferences::read);
    @Override public Type<PlayerPreferences> type() { return TYPE; }
    //?} else if forge {
    /*private static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ID, () -> "1", "1"::equals, "1"::equals);
    *///?}

    public static PlayerPreferences get(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return DEFAULTS;
        if (!serverPlayer.server.isSameThread()) throw new IllegalStateException("Preferences require server thread");
        return PLAYERS.getOrDefault(serverPlayer, DEFAULTS);
    }
    public int withdrawalCount(int stackSize, boolean sneaking) {
        return sneaking == trovesDispense ? stackSize : 1;
    }
    public void apply(Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.server.isSameThread())
            PLAYERS.put(serverPlayer, this);
    }
    public static PlayerPreferences read(FriendlyByteBuf buffer) {
        return new PlayerPreferences(buffer.readBoolean(), buffer.readBoolean());
    }
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(trovesDispense);
        buffer.writeBoolean(defaultRoutingNoNewItems);
    }
    public static void initialize() {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TYPE,
            (payload, context) -> context.server().execute(() -> payload.apply(context.player())));
        //?} else if fabric {
        /*net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(ID,
            (server, player, handler, buffer, sender) -> {
                PlayerPreferences message = read(buffer);
                if (buffer.readableBytes() == 0) server.execute(() -> message.apply(player));
            });
        *///?} else if forge {
        /*CHANNEL.registerMessage(0, PlayerPreferences.class, PlayerPreferences::write, PlayerPreferences::read,
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
    public static void send(PlayerPreferences message) {
        //? if fabric && >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(message);
        //?} else if fabric {
        /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        message.write(buffer);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ID, buffer);
        *///?} else if forge {
        /*CHANNEL.sendToServer(message);
        *///?} else {
        /*net.neoforged.neoforge.network.PacketDistributor.sendToServer(message);
        *///?}
    }
}
