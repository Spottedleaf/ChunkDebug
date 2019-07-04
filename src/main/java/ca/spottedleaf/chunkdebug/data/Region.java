package ca.spottedleaf.chunkdebug.data;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public final class Region {

    public final ObjectOpenHashSet<ChunkInformation> chunks = new ObjectOpenHashSet<>(32, 0.5f);
    public EnumMap<ChunkInformation.ChunkStatus, List<ChunkInformation>> chunksPerStatus;
    public EnumMap<TicketInformation.TicketType, List<ChunkInformation>> chunksPerTicketType;
    public List<PlayerInformation> players;

    private boolean initialized;

    public int lowerX;
    public int lowerZ;
    public int upperX;
    public int upperZ;

    public Region() {}

    public Region(final ChunkInformation initialChunk) {
        this.chunks.add(initialChunk);
    }

    public void addChunk(final ChunkInformation chunk) {
        if (!this.chunks.add(chunk)) {
            throw new IllegalStateException("Already contains chunk (" + chunk.x + "," + chunk.z + ")");
        }
    }

    public void init() {
        if (this.initialized) {
            return;
        }
        this.chunksPerStatus = new EnumMap<>(ChunkInformation.ChunkStatus.class);
        this.chunksPerTicketType = new EnumMap<>(TicketInformation.TicketType.class);
        this.initialized = true;

        boolean first = true;

        int lowerX = 0, lowerZ = 0, upperX = 0, upperZ = 0;

        for (final ChunkInformation chunk : this.chunks) {
            if (first) {
                first = false;
                lowerX = upperX = chunk.x;
                lowerZ = upperZ = chunk.z;
            } else {
                if (chunk.x < lowerX) {
                    lowerX = chunk.x;
                } else if (chunk.x > upperX) {
                    upperX = chunk.x;
                }
                if (chunk.z < lowerZ) {
                    lowerZ = chunk.z;
                } else if (chunk.z > upperZ) {
                    upperZ = chunk.z;
                }
            }

            this.chunksPerStatus.computeIfAbsent(chunk.status, (final ChunkInformation.ChunkStatus keyInMap) -> {
                return new ArrayList<>();
            }).add(chunk);
            for (final TicketInformation ticket : chunk.directTickets) {
                this.chunksPerTicketType.computeIfAbsent(ticket.ticketType, (final TicketInformation.TicketType keyInMap) -> {
                    return new ArrayList<>();
                }).add(chunk);
            }
        }

        this.lowerX = lowerX;
        this.lowerZ = lowerZ;
        this.upperX = upperX;
        this.upperZ = upperZ;
    }

    // merges this region into the specified region
    public void mergeInto(final Region other) {
        if (other == this) {
            return;
        }

        for(final ChunkInformation info : this.chunks) {
            if (other.chunks.contains(info)) {
                throw new IllegalStateException("Region merge failed: region 1: " + this.chunks.toString() + ", region 2: " + other.chunks.toString());
            }
        }


        final int chunkCount1 = this.chunks.size();
        final int chunkCount2 = other.chunks.size();

        other.chunks.addAll(this.chunks);
    }

    public static final class RegionHolder {

        public Region region;

        public RegionHolder(final Region region) {
            this.region = region;
        }
    }
}