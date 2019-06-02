package ca.spottedleaf.chunkdebug.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;

public final class WorldInformation implements Comparable<WorldInformation> {

    // serialized data
    public final String name;
    public final int viewDistance;
    public final boolean keepSpawnLoaded;
    public final int keepLoadedRange; // in blocks
    public final int visibleChunkCount;
    public final int loadedChunkCount;
    public final int verifiedFullyLoadedChunks;
    public final List<PlayerInformation> players;
    public final List<ChunkInformation> chunkInformation;

    // interpreted data
    /** Chunk by coordinate */
    public final Long2ObjectOpenHashMap<ChunkInformation> chunkInformationByCoordinate;

    /** Maps chunk status -> chunks at this status */
    public final EnumMap<ChunkInformation.ChunkStatus, List<ChunkInformation>> chunksPerStatus;

    /** Maps ticket type -> chunks with this ticket type */
    public final EnumMap<TicketInformation.TicketType, List<ChunkInformation>> chunksPerTicketType;

    /** Maps ticket type -> chunks with this ticket type or in range of this ticket range */
    public final EnumMap<TicketInformation.TicketType, Set<ChunkInformation>> associatedChunksPerTicketType;

    private WorldInformation(final String name, final int viewDistance, final boolean keepSpawnLoaded, final int keepLoadedRange,
                             final int visibleChunkCount, final int loadedChunkCount, final int verifiedFullyLoadedChunks,
                             final List<PlayerInformation> players, final List<ChunkInformation> chunkInformation,
                             final Long2ObjectOpenHashMap<ChunkInformation> chunkInformationByCoordinate,
                             final EnumMap<ChunkInformation.ChunkStatus, List<ChunkInformation>> chunksPerStatus,
                             final EnumMap<TicketInformation.TicketType, List<ChunkInformation>> chunksPerTicketType,
                             final EnumMap<TicketInformation.TicketType, Set<ChunkInformation>> associatedChunksPerTicketType) {
        this.name = name;
        this.viewDistance = viewDistance;
        this.keepSpawnLoaded = keepSpawnLoaded;
        this.keepLoadedRange = keepLoadedRange;
        this.visibleChunkCount = visibleChunkCount;
        this.loadedChunkCount = loadedChunkCount;
        this.verifiedFullyLoadedChunks = verifiedFullyLoadedChunks;
        this.players = players;
        this.chunkInformation = chunkInformation;
        this.chunkInformationByCoordinate = chunkInformationByCoordinate;
        this.chunksPerStatus = chunksPerStatus;
        this.chunksPerTicketType = chunksPerTicketType;
        this.associatedChunksPerTicketType = associatedChunksPerTicketType;
    }

    @Override
    public int compareTo(final WorldInformation other) {
        return this.name.compareTo(other.name);
    }

    public ChunkInformation getChunkInformation(final int x, final int z) {
        return this.chunkInformationByCoordinate.get(DataUtil.getCoordinateKey(x, z));
    }

    // see https://github.com/PaperMC/Paper/pull/2118/files
    public static WorldInformation loadFromJson(final JsonObject object) {
        // load data

        final String name = object.get("name").getAsString();
        final int viewDistance = object.get("view-distance").getAsInt();
        final boolean keepSpawnLoaded = object.get("keep-spawn-loaded").getAsBoolean();
        final int keepSpawnLoadedRange = object.get("keep-spawn-loaded-range").getAsInt();
        final int visibleChunkCount = object.get("visible-chunk-count").getAsInt();
        final int loadedChunkCount = object.get("loaded-chunk-count").getAsInt();
        final int verifiedFullyLoadedChunks = object.get("verified-fully-loaded-chunks").getAsInt();

        final JsonArray playersData = object.get("players").getAsJsonArray();
        final JsonArray chunksData = object.get("chunk-data").getAsJsonArray();

        final List<PlayerInformation> players = new ArrayList<>(playersData.size());
        final List<ChunkInformation> chunks = new ArrayList<>(chunksData.size());
        final Long2ObjectOpenHashMap<ChunkInformation> chunkMap = new Long2ObjectOpenHashMap<>(chunks.size(), 0.5f);
        final EnumMap<ChunkInformation.ChunkStatus, List<ChunkInformation>> chunksPerStatus = new EnumMap<>(ChunkInformation.ChunkStatus.class);
        final EnumMap<TicketInformation.TicketType, List<ChunkInformation>> chunksPerTicketType = new EnumMap<>(TicketInformation.TicketType.class);
        final EnumMap<TicketInformation.TicketType, Set<ChunkInformation>> associatedChunksPerTicketType = new EnumMap<>(TicketInformation.TicketType.class);

        for (final JsonElement player : playersData) {
            players.add(PlayerInformation.fromJson(player.getAsJsonObject()));
        }

        for (final JsonElement chunk : chunksData) {
            final ChunkInformation information = ChunkInformation.fromJson(chunk.getAsJsonObject());
            final long key = DataUtil.getCoordinateKey(information.x, information.z);

            if (chunkMap.putIfAbsent(key, information) != null) {
                throw new IllegalStateException("Contains duplicate chunk (" + information.x + "," + information.z + ") in world '" + name + "'");
            }

            chunksPerStatus.computeIfAbsent(information.status, (final ChunkInformation.ChunkStatus keyInMap) -> new ArrayList<>()).add(information);

            for (final TicketInformation ticket : information.directTickets) {
                chunksPerTicketType.computeIfAbsent(ticket.ticketType, (final TicketInformation.TicketType keyInMap) -> new ArrayList<>()).add(information);
            }

            chunks.add(information);
        }

        // map out ticket relations

        // we need to do this after loading all chunks since we want a reference to chunk informations



        for (final ChunkInformation chunk : chunks) {
            final int originX = chunk.x;
            final int originZ = chunk.z;
            for (final TicketInformation ticket : chunk.directTickets) {
                final int ticketRadius = DataUtil.getSquareRadiusForTicket(ticket.ticketLevel);

                // iterate over the square
                for (int dx = -ticketRadius; dx <= ticketRadius; ++dx) {
                    for (int dz = -ticketRadius; dz <= ticketRadius; ++dz) {
                        final ChunkInformation other = chunkMap.get(DataUtil.getCoordinateKey(originX + dx, originZ + dz));
                        if (other == null) {
                            // TODO is this an error?
                            continue;
                        }

                        other.ticketsInRangeOf.add(ticket);
                        ticket.affectingChunks.add(other);
                        associatedChunksPerTicketType.computeIfAbsent(ticket.ticketType, (final TicketInformation.TicketType keyInMap) -> {
                            return new HashSet<>();
                        }).add(other);
                    }
                }
            }
        }

        return new WorldInformation(name, viewDistance, keepSpawnLoaded, keepSpawnLoadedRange, visibleChunkCount, loadedChunkCount,
                                    verifiedFullyLoadedChunks, players, chunks, chunkMap, chunksPerStatus, chunksPerTicketType, associatedChunksPerTicketType);
    }

}
