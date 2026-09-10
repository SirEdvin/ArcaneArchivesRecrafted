package com.aranaira.arcanearchives.events;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class GemSoundTest {
    @Test void everyImplementedEffectRoundTripsAsOneByte() {
        assertEquals(3, GemSound.Effect.values().length);
        for (var effect : GemSound.Effect.values()) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                var original = new GemSound(effect);
                GemSound.CODEC.encode(buffer, original);
                assertEquals(1, buffer.readableBytes());
                assertEquals(original, GemSound.CODEC.decode(buffer));
                assertEquals(0, buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }
    @Test void malformedEffectsAreNotPlayableMessages() {
        assertThrows(NullPointerException.class, () -> new GemSound(null));
        for (int value : new int[]{-1, 3, Integer.MAX_VALUE}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                buffer.writeVarInt(value);
                assertThrows(RuntimeException.class, () -> GemSound.read(buffer));
            } finally { buffer.release(); }
        }
        var empty = new FriendlyByteBuf(Unpooled.buffer());
        try { assertThrows(RuntimeException.class, () -> GemSound.read(empty)); }
        finally { empty.release(); }
    }
}
*///?}
