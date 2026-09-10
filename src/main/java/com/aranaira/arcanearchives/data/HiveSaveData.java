package com.aranaira.arcanearchives.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
//?}

/** Server-owned membership. Network storage integration is separate from this saved membership. */
public final class HiveSaveData extends SavedData {
    private final Map<UUID, LinkedHashSet<UUID>> hives = new LinkedHashMap<>();

    public static HiveSaveData get(MinecraftServer server) {
        if (!server.isSameThread()) throw new IllegalStateException("Hive data requires the server thread");
        //? if >=1.21 {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            HiveSaveData::new, (tag, registries) -> load(tag), DataFixTypes.LEVEL), "arcanearchives-hives");
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(HiveSaveData::load,
            HiveSaveData::new, "arcanearchives-hives");
        *///?}
    }

    public UUID ownerOf(UUID member) {
        Objects.requireNonNull(member, "member");
        if (hives.containsKey(member)) return member;
        for (var entry : hives.entrySet()) if (entry.getValue().contains(member)) return entry.getKey();
        return null;
    }

    public Set<UUID> members(UUID owner) {
        var members = hives.get(owner);
        return members == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(members));
    }

    /** Validate both people before creating anything, including a previously unaffiliated author's Hive. */
    public boolean acceptInvitation(UUID author, UUID recipient) {
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(recipient, "recipient");
        UUID currentOwner = ownerOf(author);
        if (author.equals(recipient) || (currentOwner != null && !author.equals(currentOwner))
                || ownerOf(recipient) != null) return false;
        hives.computeIfAbsent(author, ignored -> new LinkedHashSet<>()).add(recipient);
        setDirty();
        return true;
    }

    /** Upstream leaves the oldest remaining member in charge; a lone player is not a Hive. */
    public boolean resign(UUID member) {
        UUID owner = ownerOf(member);
        if (owner == null) return false;
        LinkedHashSet<UUID> members = hives.get(owner);
        if (owner.equals(member)) {
            var order = members.iterator();
            UUID successor = order.next(); // Valid stored Hives always have at least one nonowner member.
            order.remove();
            hives.remove(owner);
            if (!members.isEmpty()) hives.put(successor, members);
        } else {
            members.remove(member);
            if (members.isEmpty()) hives.remove(owner);
        }
        setDirty();
        return true;
    }

    public static HiveSaveData load(CompoundTag tag) {
        HiveSaveData result = new HiveSaveData();
        if (!tag.contains("hive_data", Tag.TAG_LIST)) throw new IllegalArgumentException("Missing Hive data");
        ListTag list = (ListTag) tag.get("hive_data");
        if (!list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND)
            throw new IllegalArgumentException("Invalid Hive list");
        Set<UUID> identities = new LinkedHashSet<>();
        for (Tag value : list) {
            CompoundTag hive = (CompoundTag) value;
            if (!hive.hasUUID("owner") || !hive.contains("members", Tag.TAG_LIST))
                throw new IllegalArgumentException("Invalid Hive record");
            UUID owner = hive.getUUID("owner");
            if (!identities.add(owner)) throw new IllegalArgumentException("Overlapping Hive identity");
            ListTag savedMembers = (ListTag) hive.get("members");
            if (savedMembers.isEmpty() || savedMembers.getElementType() != Tag.TAG_COMPOUND)
                throw new IllegalArgumentException("Invalid Hive members");
            LinkedHashSet<UUID> members = new LinkedHashSet<>();
            for (Tag memberValue : savedMembers) {
                CompoundTag member = (CompoundTag) memberValue;
                if (!member.hasUUID("uuid") || !identities.add(member.getUUID("uuid")))
                    throw new IllegalArgumentException("Invalid or overlapping Hive member");
                members.add(member.getUUID("uuid"));
            }
            result.hives.put(owner, members);
        }
        return result;
    }

    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else {
    /*public CompoundTag save(CompoundTag tag) {
    *///?}
        ListTag list = new ListTag();
        hives.forEach((owner, members) -> {
            CompoundTag hive = new CompoundTag();
            hive.putUUID("owner", owner);
            ListTag savedMembers = new ListTag();
            for (UUID member : members) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("uuid", member);
                savedMembers.add(entry);
            }
            hive.put("members", savedMembers);
            list.add(hive);
        });
        tag.put("hive_data", list);
        return tag;
    }
}
