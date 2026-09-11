package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.config.ClientConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** One positional native loop per client resonator; never owns world lifetime. */
public final class ResonatorLoopSound extends AbstractTickableSoundInstance {
    private final WeakReference<RadiantResonatorBlockEntity> resonator;

    private ResonatorLoopSound(RadiantResonatorBlockEntity entity) {
        super(ContentRegistry.RESONATOR_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        resonator = new WeakReference<>(entity);
        x = entity.getBlockPos().getX() + .5;
        y = entity.getBlockPos().getY() + .5;
        z = entity.getBlockPos().getZ() + .5;
        volume = ClientConfig.current().resonatorVolume();
        pitch = 1F;
        looping = true;
    }

    public static void initialize() {
        RadiantResonatorBlockEntity.clientSoundFactory = entity -> {
            ResonatorLoopSound[] active = new ResonatorLoopSound[1];
            return () -> {
                var manager = Minecraft.getInstance().getSoundManager();
                if (!eligible(entity)) {
                    if (active[0] != null) {
                        active[0].stop();
                        manager.stop(active[0]);
                        active[0] = null;
                    }
                } else if (active[0] == null || !manager.isActive(active[0])) {
                    active[0] = new ResonatorLoopSound(entity);
                    manager.play(active[0]);
                }
            };
        };
    }

    private static boolean eligible(RadiantResonatorBlockEntity entity) {
        var client = Minecraft.getInstance();
        var config = ClientConfig.current();
        return entity != null && client.level != null && entity.getLevel() == client.level
            && config.useSounds() && config.resonatorTicking() && config.resonatorVolume() > 0F
            && entity.isResonating();
    }

    @Override public void tick() {
        if (!eligible(resonator.get())) stop();
    }
}
