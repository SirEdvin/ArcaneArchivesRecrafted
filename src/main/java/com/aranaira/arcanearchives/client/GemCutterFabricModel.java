package com.aranaira.arcanearchives.client;

//? if fabric {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Fabric's vanilla-quad adapter for the release OBJ; Forge-family uses its native OBJ loader. */
public final class GemCutterFabricModel implements BakedModel {
    private final BakedModel delegate;
    private final List<BakedQuad> quads;
    private final boolean ambientOcclusion;

    public GemCutterFabricModel(BakedModel delegate, ModelState state, Function<Material, TextureAtlasSprite> textures) {
        this(delegate, state, textures, "gemcutters_table");
    }

    public GemCutterFabricModel(BakedModel delegate, ModelState state, Function<Material, TextureAtlasSprite> textures, String name) {
        this(delegate, state, textures, name, name, true);
    }

    public GemCutterFabricModel(BakedModel delegate, ModelState state, Function<Material, TextureAtlasSprite> textures,
            String name, String materialName, boolean ambientOcclusion) {
        this.delegate = delegate;
        this.ambientOcclusion = ambientOcclusion;
        this.quads = read(state, textures, name, materialName);
    }

    static ResourceLocation location(String name) {
        //? if >=1.21 {
        return ResourceLocation.parse(name);
        //?} else {
        /*return new ResourceLocation(name);
        *///?}
    }

    private static BufferedReader open(String path) throws IOException {
        return Minecraft.getInstance().getResourceManager()
            .getResourceOrThrow(location("arcanearchives:models/block/" + path)).openAsReader();
    }

    private static List<BakedQuad> read(ModelState state, Function<Material, TextureAtlasSprite> textures, String name, String materialName) {
        List<Vector3f> positions = new ArrayList<>();
        List<float[]> coordinates = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        Map<String, TextureAtlasSprite> materials = new HashMap<>();
        List<BakedQuad> result = new ArrayList<>();
        try (BufferedReader reader = open(materialName + ".mtl")) {
            String material = null;
            for (String line; (line = reader.readLine()) != null;) {
                String[] words = line.trim().split("\\s+");
                if (words[0].equals("newmtl")) material = words[1];
                if (words[0].equals("map_Kd")) {
                    materials.put(material, textures.apply(new Material(TextureAtlas.LOCATION_BLOCKS, location(words[1]))));
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot load Gem Cutter materials", exception);
        }
        try (BufferedReader reader = open(name + ".obj")) {
            TextureAtlasSprite sprite = null;
            for (String line; (line = reader.readLine()) != null;) {
                String[] words = line.trim().split("\\s+");
                switch (words[0]) {
                    case "v" -> {
                        Vector3f position = vector(words);
                        position.sub(0.5F, 0.5F, 0.5F);
                        state.getRotation().getMatrix().transformPosition(position);
                        positions.add(position.add(0.5F, 0.5F, 0.5F));
                    }
                    case "vt" -> coordinates.add(new float[]{Float.parseFloat(words[1]), 1 - Float.parseFloat(words[2])});
                    case "vn" -> {
                        Vector3f normal = vector(words);
                        state.getRotation().getMatrix().transformDirection(normal);
                        normals.add(normal.normalize());
                    }
                    case "usemtl" -> {
                        sprite = materials.get(words[1]);
                        if (sprite == null) throw new IllegalArgumentException("Unknown Gem Cutter material: " + words[1]);
                    }
                    case "f" -> {
                        if (sprite == null || words.length < 4 || words.length > 5) {
                            throw new IllegalArgumentException("Expected a textured Gem Cutter triangle or quad: " + line);
                        }
                        int[] vertices = new int[32];
                        Vector3f faceNormal = null;
                        for (int vertex = 0; vertex < 4; vertex++) {
                            // A repeated last vertex represents an OBJ triangle in the native quad pipeline.
                            String[] indices = words[Math.min(vertex + 1, words.length - 1)].split("/");
                            Vector3f position = positions.get(Integer.parseInt(indices[0]) - 1);
                            float[] uv = coordinates.get(Integer.parseInt(indices[1]) - 1);
                            Vector3f normal = normals.get(Integer.parseInt(indices[2]) - 1);
                            if (faceNormal == null) faceNormal = normal;
                            int offset = vertex * 8;
                            vertices[offset] = Float.floatToRawIntBits(position.x());
                            vertices[offset + 1] = Float.floatToRawIntBits(position.y());
                            vertices[offset + 2] = Float.floatToRawIntBits(position.z());
                            vertices[offset + 3] = -1;
                            //? if >=1.21 {
                            vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU(uv[0]));
                            vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV(uv[1]));
                            //?} else {
                            /*vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU(uv[0] * 16));
                            vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV(uv[1] * 16));
                            *///?}
                            vertices[offset + 7] = (Math.round(normal.x() * 127) & 255)
                                | ((Math.round(normal.y() * 127) & 255) << 8)
                                | ((Math.round(normal.z() * 127) & 255) << 16);
                        }
                        result.add(new BakedQuad(vertices, -1,
                            Direction.getNearest(faceNormal.x(), faceNormal.y(), faceNormal.z()), sprite, true));
                    }
                    case "", "#", "o", "g", "s", "mtllib" -> { }
                    default -> throw new IllegalArgumentException("Unsupported Gem Cutter OBJ record: " + line);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot load Gem Cutter geometry", exception);
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Gem Cutter OBJ has no faces");
        return List.copyOf(result);
    }

    private static Vector3f vector(String[] words) {
        return new Vector3f(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3]));
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return side == null ? quads : List.of();
    }

    @Override public boolean useAmbientOcclusion() { return ambientOcclusion; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }
}
//?}
