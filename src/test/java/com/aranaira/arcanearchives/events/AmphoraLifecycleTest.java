package com.aranaira.arcanearchives.events;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.DispenseAmphora;
import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import net.minecraft.world.level.block.DispenserBlock;
import org.junit.jupiter.api.Test;

class AmphoraLifecycleTest {
    @Test void startupDoesNotOverwriteRegisteredDispenserBehavior() {
        var item = ContentRegistry.RADIANT_AMPHORA.get();
        var behavior = DispenserBlock.DISPENSER_REGISTRY.get(item);
        assertInstanceOf(DispenseAmphora.class, behavior);
        // Exercise the production callback without starting a world or fabricating a server.
        // Null is sufficient here: registration must be independent of server identity.
        AmphoraEvents.started(null);
        assertSame(behavior, DispenserBlock.DISPENSER_REGISTRY.get(item));
        RadiantAmphoraItem.serverStopped(null);
        AmphoraEvents.started(null);
        assertSame(behavior, DispenserBlock.DISPENSER_REGISTRY.get(item));
    }
}
*///?}
