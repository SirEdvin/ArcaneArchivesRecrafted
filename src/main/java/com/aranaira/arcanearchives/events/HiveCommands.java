package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.data.HiveSaveData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Original player-only /hive diagnostics, with no operator requirement. */
public final class HiveCommands {
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hive").executes(context -> {
            if (!(context.getSource().getEntity() instanceof ServerPlayer player)) return 0;
            HiveSaveData data = HiveSaveData.get(player.server);
            var owner = data.ownerOf(player.getUUID());
            if (owner == null) {
                player.sendSystemMessage(Component.literal("You are not in a hive."));
                return 1;
            }
            if (owner.equals(player.getUUID())) {
                player.sendSystemMessage(Component.literal("You own this hive network."));
            } else {
                var profile = player.server.getProfileCache().get(owner);
                player.sendSystemMessage(Component.literal(profile.isEmpty()
                    ? "Cannot resolve owner for hive network."
                    : "You are in the Hive owned by: " + profile.get().getName()));
            }
            player.sendSystemMessage(Component.literal("UUID for network is: " + owner));
            var members = data.members(owner);
            player.sendSystemMessage(Component.literal("Hive has " + members.size() + " members."));
            for (var member : members) {
                String name = player.server.getProfileCache().get(member)
                    .map(com.mojang.authlib.GameProfile::getName).orElse("Unknown name");
                player.sendSystemMessage(Component.literal("Hive member " + name + " and ID: " + member));
            }
            return 1;
        }));
    }

    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
            (dispatcher, access, environment) -> register(dispatcher));
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.RegisterCommandsEvent event) -> register(event.getDispatcher()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.RegisterCommandsEvent event) -> register(event.getDispatcher()));
        *///?}
    }
    private HiveCommands() {}
}
