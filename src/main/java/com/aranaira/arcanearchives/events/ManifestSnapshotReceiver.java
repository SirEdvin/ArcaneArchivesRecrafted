package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.data.ManifestContents;
import java.io.ByteArrayOutputStream;
import java.util.List;

/** Shared bounded assembly for menu listings and connection-scoped tracking snapshots. */
public final class ManifestSnapshotReceiver {
    private final int containerId;
    private long snapshotRevision = -1;
    private List<ManifestContents.Entry> entries = List.of();
    private ByteArrayOutputStream receiving;
    private int expectedBytes;
    private boolean failed;
    private boolean ready;

    public ManifestSnapshotReceiver(int containerId) { this.containerId = containerId; }
    public List<ManifestContents.Entry> entries() { return entries; }
    public long snapshotRevision() { return snapshotRevision; }
    public boolean failed() { return failed; }
    public boolean ready() { return ready; }
    public void receive(ManifestSnapshot fragment, net.minecraft.core.HolderLookup.Provider registries) {
        if (fragment.containerId() != containerId) return;
        if (fragment.revision() < snapshotRevision
                || fragment.revision() == snapshotRevision && receiving == null) return;
        if (fragment.revision() > snapshotRevision) {
            snapshotRevision = fragment.revision();
            receiving = null;
            if (fragment.offset() != 0) { rejectSnapshot(); return; }
        } else if (fragment.offset() == 0) return;
        byte[] data = fragment.data();
        if (fragment.totalBytes() == 0) { rejectSnapshot(); return; }
        if (fragment.offset() == 0) {
            receiving = new ByteArrayOutputStream();
            expectedBytes = fragment.totalBytes();
            entries = List.of();
            failed = false;
            ready = false;
        }
        if (receiving == null || fragment.totalBytes() != expectedBytes || fragment.offset() != receiving.size()) {
            rejectSnapshot();
            return;
        }
        receiving.writeBytes(data);
        if (receiving.size() == expectedBytes) {
            try {
                entries = ManifestSnapshot.decode(receiving.toByteArray(), registries);
                failed = false;
                ready = true;
                receiving = null;
            } catch (RuntimeException error) {
                ArcaneArchivesMod.LOGGER.warn("Rejected invalid Manifest snapshot", error);
                rejectSnapshot();
            }
        }
    }
    private void rejectSnapshot() {
        receiving = null;
        entries = List.of();
        failed = true;
        ready = true;
    }
}
