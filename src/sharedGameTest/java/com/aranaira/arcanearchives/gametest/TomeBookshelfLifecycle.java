package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.PlayerSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Actual native game-mode breaks; no direct invocation of the grant callback. */
public final class TomeBookshelfLifecycle {
    public static void run(GameTestHelper helper, Function<GameTestHelper, ServerPlayer> factory) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        require(level.isEmptyBlock(pos), "Bookshelf fixture occupied");
        for (var mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
            var player = factory.apply(helper);
            player.setGameMode(mode);
            player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 2);
            var receipt = PlayerSaveData.get(level.getServer(), player.getUUID());
            require(!receipt.hasReceivedBook(), "Bookshelf fixture requires a fresh player receipt");
            var area = new AABB(pos).inflate(4);
            var oldEntities = level.getEntitiesOfClass(ItemEntity.class, area).stream().map(ItemEntity::getUUID).toList();
            try {
                for (var block : new net.minecraft.world.level.block.Block[]{Blocks.STONE, Blocks.CHISELED_BOOKSHELF}) {
                    level.setBlockAndUpdate(pos, block.defaultBlockState());
                    require(player.gameMode.destroyBlock(pos) && level.isEmptyBlock(pos), "Ineligible native break failed");
                    require(!receipt.hasReceivedBook(), "Unrelated/chiseled bookshelf granted a Tome");
                }
                for (int attempt = 0; attempt < 2; attempt++) {
                    level.setBlockAndUpdate(pos, Blocks.BOOKSHELF.defaultBlockState());
                    require(player.gameMode.destroyBlock(pos) && level.isEmptyBlock(pos), "Native bookshelf break failed");
                    var tomes = level.getEntitiesOfClass(ItemEntity.class, area, e -> !oldEntities.contains(e.getUUID())
                        && e.getItem().is(ContentRegistry.TOME_OF_ARCANA.get()));
                    require(receipt.hasReceivedBook() && tomes.size() == 1 && tomes.get(0).getItem().getCount() == 1
                        && !tomes.get(0).hasPickUpDelay(), "Bookshelf grant missing, duplicated or delayed in " + mode);
                }
            } finally {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.getEntitiesOfClass(ItemEntity.class, area, e -> !oldEntities.contains(e.getUUID())).forEach(ItemEntity::discard);
            }
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
