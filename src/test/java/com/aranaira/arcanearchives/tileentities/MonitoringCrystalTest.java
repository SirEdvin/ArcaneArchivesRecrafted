package com.aranaira.arcanearchives.tileentities;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.blocks.MonitoringCrystal;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.junit.jupiter.api.Test;

class MonitoringCrystalTest {
    @Test void allFacesTargetOppositeNeighborAndHaveNoncollidingShape() {
        var block = ContentRegistry.MONITORING_CRYSTAL.get();
        var pos = new BlockPos(10, 20, 30);
        for (Direction facing : Direction.values()) {
            var state = block.defaultBlockState().setValue(MonitoringCrystal.FACING, facing);
            var tile = new MonitoringCrystalBlockEntity(pos, state);
            assertEquals(pos.relative(facing.getOpposite()), tile.target());
            assertNull(tile.targetTile());
            assertNull(tile.inventory());
            assertFalse(state.getShape(null, pos).isEmpty());
            assertTrue(state.getCollisionShape(null, pos, CollisionContext.empty()).isEmpty());
            assertEquals(15, state.getLightEmission());
            assertTrue(MonitoringCrystalBlockEntity.isArcaneDevice(tile));
        }
    }
    @Test void crystalRejectsMigratedStorageButNotOrdinaryChest() {
        assertTrue(MonitoringCrystalBlockEntity.isArcaneDevice(new RadiantChestBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_CHEST.get().defaultBlockState())));
        assertFalse(MonitoringCrystalBlockEntity.isArcaneDevice(new net.minecraft.world.level.block.entity.ChestBlockEntity(BlockPos.ZERO, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())));
        assertFalse(MonitoringCrystalBlockEntity.isArcaneDevice(null));
    }
}
*///?}
