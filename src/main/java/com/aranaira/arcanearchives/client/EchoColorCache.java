package com.aranaira.arcanearchives.client;

import net.minecraft.resources.ResourceLocation;
import com.aranaira.arcanearchives.util.EchoOreInputs;
import com.aranaira.arcanearchives.util.EchoTintCache;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lazy only until a connected client's recipes, tags and baked models are available. */
public final class EchoColorCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoColorCache.class);
    private static EchoTintCache cache;
    private static boolean generated;

    public static void initialize(Path configDirectory) {
        try {
            cache = new EchoTintCache(configDirectory.resolve("arcanearchives"));
            generated = cache.hasColors();
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load Echo configuration; existing files were not replaced", error);
        }
    }

    public static int color(ItemStack source) {
        if (cache == null) return -1;
        Minecraft client = Minecraft.getInstance();
        if (!generated && client.level != null) {
            generated = true; // Even an empty result must not rescan or rewrite files every render frame.
            generate(client);
        }
        return cache.color(BuiltInRegistries.ITEM.getKey(source.getItem()).toString());
    }

    private static void generate(Minecraft client) {
        var level = client.level;
        //? if >=1.21 {
        var recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream().map(holder -> holder.value()).toList();
        //?} else {
        /*var recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
        *///?}
        try {
            if (cache.inputs().isEmpty()) cache.generateInputs(EchoOreInputs.discover(recipes));
        } catch (IOException error) {
            LOGGER.warn("Unable to write Echo ore inputs; using this session's generated inputs", error);
        }
        var colors = new LinkedHashMap<String, Integer>();
        for (String id : cache.inputs()) {
            var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(id));
            if (item.isEmpty()) continue;
            ItemStack input = new ItemStack(item.get());
            if (input.isEmpty()) continue;
            //? if >=1.21 {
            var match = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new net.minecraft.world.item.crafting.SingleRecipeInput(input), level);
            if (match.isEmpty()) continue;
            var recipe = match.get().value();
            //?} else {
            /*var match = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.SimpleContainer(input), level);
            if (match.isEmpty()) continue;
            var recipe = match.get();
            *///?}
            ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
            if (!output.isEmpty()) {
                Integer color = sample(client, output);
                if (color != null) colors.put(BuiltInRegistries.ITEM.getKey(output.getItem()).toString(), color);
            }
        }
        try {
            cache.generateColors(colors);
        } catch (IOException error) {
            LOGGER.warn("Unable to write Echo colors; using this session's generated colors", error);
        }
    }

    private static Integer sample(Minecraft client, ItemStack output) {
        var model = client.getItemRenderer().getModel(output, null, null, 0);
        for (var quad : model.getQuads(null, null, RandomSource.create(0))) {
            if (quad.getDirection() != Direction.SOUTH) continue;
            var sprite = quad.getSprite().contents().name();
            var resource = ResourceLocation.tryParse(sprite.getNamespace() + ":textures/" + sprite.getPath() + ".png");
            try (var stream = client.getResourceManager().open(resource)) {
                return EchoTintCache.sample(ImageIO.read(stream));
            } catch (IOException error) {
                LOGGER.debug("Unable to sample Echo texture {}", resource, error);
                return null;
            }
        }
        return null;
    }

    private EchoColorCache() {}
}
