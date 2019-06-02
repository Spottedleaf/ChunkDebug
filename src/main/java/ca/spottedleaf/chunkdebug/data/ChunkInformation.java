package ca.spottedleaf.chunkdebug.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChunkInformation implements Comparable<ChunkInformation> {

    // serialized data
    public final int x;
    public final int z;
    public final int ticketLevel;
    public final boolean queuedUnload;
    public final ChunkStatus status;
    public final List<TicketInformation> directTickets;

    // interpreted data
    public final List<TicketInformation> ticketsInRangeOf; // tickets affecting this chunk

    private ChunkInformation(final int x, final int z, final int ticketLevel, final boolean queuedUnload, final ChunkStatus status, final List<TicketInformation> directTickets,
                            final List<TicketInformation> ticketsInRangeOf) {
        this.x = x;
        this.z = z;
        this.ticketLevel = ticketLevel;
        this.queuedUnload = queuedUnload;
        this.status = status;
        this.directTickets = directTickets;
        this.ticketsInRangeOf = ticketsInRangeOf;
    }

    @Override
    public int compareTo(final ChunkInformation other) {
        int compare = Integer.compare(this.x, other.x);
        return compare == 0 ? Integer.compare(this.z, other.z) : compare;
    }

    public static ChunkInformation fromJson(final JsonObject object) {
        final int x = object.get("x").getAsInt();
        final int z = object.get("z").getAsInt();
        final int ticketLevel = object.get("ticket-level").getAsInt();
        //final String state = object.get("state").getAsString(); // unused
        final boolean queuedForUnload = object.get("queued-for-unload").getAsBoolean();
        final ChunkStatus status = ChunkStatus.getByString(object.get("status").getAsString());

        final JsonArray ticketsData = object.get("tickets").getAsJsonArray();
        final List<TicketInformation> directTickets = new ArrayList<>(ticketsData.size());

        for (final JsonElement ticketData : ticketsData) {
            directTickets.add(TicketInformation.from(ticketData.getAsJsonObject(), x, z));
        }

        return new ChunkInformation(x, z, ticketLevel, queuedForUnload, status, directTickets, new ArrayList<>());
    }

    public static enum ChunkStatus {
        UNLOADED,
        EMPTY,
        STRUCTURE_STARTS,
        STRUCTURE_REFERENCES,
        BIOMES,
        NOISE,
        SURFACE,
        CARVERS,
        LIQUID_CARVERS,
        FEATURES,
        LIGHT,
        SPAWN,
        HEIGHTMAPS,
        FULL;

        public static ChunkStatus getByString(String status) {
            if (status.startsWith("minecraft:")) {
                status = status.substring("minecraft:".length());
            }
            return ChunkStatus.valueOf(status.toUpperCase(Locale.ENGLISH));
        }
    }
}