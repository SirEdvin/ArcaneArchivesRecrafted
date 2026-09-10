package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.GemSocketMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Live active references in upstream order; ordinary inventory sockets are not passive equipment. */
public final class AvailableGems {
    private AvailableGems() {}
    public static List<ItemStack> get(Player player) {
        List<ItemStack> gems = new ArrayList<>();
        if (player.getMainHandItem().getItem() instanceof ArcaneGemItem) gems.add(player.getMainHandItem());
        if (player.getOffhandItem().getItem() instanceof ArcaneGemItem) gems.add(player.getOffhandItem());
        if (player.containerMenu instanceof GemSocketMenu menu) {
            WornGemSocket.forget(player);
            if (menu.stillValid(player) && menu.gem().getItem() instanceof ArcaneGemItem) gems.add(menu.gem());
        } else {
            ItemStack worn = WornGemSocket.gem(player);
            if (worn.getItem() instanceof ArcaneGemItem) gems.add(worn);
        }
        return gems;
    }
    public static boolean contains(Player player, ItemStack gem) {
        return get(player).stream().anyMatch(candidate -> candidate == gem);
    }
    public static void changed(Player player) {
        if (player.level().isClientSide) return;
        if (player.containerMenu instanceof GemSocketMenu menu) menu.save();
        else WornGemSocket.save(player);
        player.getInventory().setChanged();
    }
}
