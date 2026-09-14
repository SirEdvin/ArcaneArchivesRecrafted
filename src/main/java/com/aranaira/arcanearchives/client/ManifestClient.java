package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.events.ManifestSnapshot;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import net.minecraft.client.Minecraft;

public final class ManifestClient {
    private ManifestClient() {}
    private static net.minecraft.client.multiplayer.ClientPacketListener connection;
    private static net.minecraft.client.player.LocalPlayer preferencePlayer;
    private static com.aranaira.arcanearchives.events.ManifestSnapshotReceiver tracking =
        new com.aranaira.arcanearchives.events.ManifestSnapshotReceiver(ManifestSnapshot.TRACKING_ID);

    public static void tick() {
        var current = Minecraft.getInstance().getConnection();
        if (connection != current) {
            connection = current;
            preferencePlayer = null;
            tracking = new com.aranaira.arcanearchives.events.ManifestSnapshotReceiver(ManifestSnapshot.TRACKING_ID);
        }
        var player = Minecraft.getInstance().player;
        if (current != null && player != null && preferencePlayer != player) {
            var config = com.aranaira.arcanearchives.config.ClientConfig.current();
            com.aranaira.arcanearchives.events.PlayerPreferences.send(new com.aranaira.arcanearchives.events.PlayerPreferences(
                config.trovesDispense(), config.defaultRoutingNoNewItems()));
            preferencePlayer = player;
        }
    }
    public static java.util.List<com.aranaira.arcanearchives.data.ManifestContents.Entry> tracking() {
        tick();
        return tracking.entries();
    }
    public static void receive(ManifestSnapshot message, net.minecraft.network.Connection source) {
        tick();
        if (connection == null || connection.getConnection() != source) return;
        var player = Minecraft.getInstance().player;
        if (player != null && connection != null && message.containerId() == ManifestSnapshot.TRACKING_ID) {
            tracking.receive(message, player.level().registryAccess());
            return;
        }
        if (player != null && player.containerMenu instanceof ManifestMenu menu
                && menu.containerId == message.containerId()) menu.receive(message);
    }
    //? if fabric {
    public static void initialize() {
        //? if >=1.21 {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(ManifestSnapshot.TYPE,
            (message, context) -> {
                var listener = context.client().getConnection();
                var source = listener == null ? null : listener.getConnection();
                context.client().execute(() -> receive(message, source));
            });
        //?} else {
        /*net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(ManifestSnapshot.ID,
            (client, handler, buffer, sender) -> {
                ManifestSnapshot message = ManifestSnapshot.read(buffer);
                if (buffer.readableBytes() == 0) client.execute(() -> receive(message, handler.getConnection()));
            });
        *///?}
    }
    //?}
}
