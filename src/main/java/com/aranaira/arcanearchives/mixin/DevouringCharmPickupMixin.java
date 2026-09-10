package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.DevouringCharmItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class DevouringCharmPickupMixin {
    @Inject(method = "playerTouch", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private void arcanearchives$voidPickup(Player player, CallbackInfo callback) {
        if (DevouringCharmItem.voidPickup(player, (ItemEntity) (Object) this)) callback.cancel();
    }
}
//?}
