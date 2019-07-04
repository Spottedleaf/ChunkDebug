package ca.spottedleaf.chunkdebug.data;

import com.google.gson.JsonObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TicketInformation implements Comparable<TicketInformation> {

    // serialized data
    public final TicketType ticketType;
    public final int ticketLevel;
    public final String reason;
    public final long addTick;

    // interpreted data
    public final int x;
    public final int z;
    public final List<ChunkInformation> affectingChunks;

    private TicketInformation(final TicketType ticketType, final int ticketLevel, final String reason, final long addTick, final int x, final int z) {
        this.ticketType = ticketType;
        this.ticketLevel = ticketLevel;
        this.reason = reason;
        this.addTick = addTick;
        this.x = x;
        this.z = z;
        this.affectingChunks = new ArrayList<>(DataUtil.getTotalChunksForTicket(ticketLevel));
    }

    @Override
    public int compareTo(final TicketInformation other) {
        int compare = Integer.compare(this.x, other.x);
        return compare == 0 ? Integer.compare(this.z, other.z) : compare;
    }

    public static TicketInformation from(final JsonObject object, final int x, final int z) {
        final TicketType ticketType = TicketType.getByString(object.get("ticket-type").getAsString());
        final int ticketLevel = object.get("ticket-level").getAsInt();
        final long creationTick = object.get("add-tick").getAsLong();
        final String objectReason = object.get("object-reason").getAsString();

        return new TicketInformation(ticketType, ticketLevel, objectReason, creationTick, x, z);
    }


    public static enum TicketType {
        // in general: perma loaded -> red, temporary -> magenta
        START(Color.RED),
        DRAGON(Color.MAGENTA),
        PLAYER(Color.YELLOW),
        FORCED(Color.RED),
        LIGHT(Color.MAGENTA),
        PORTAL(Color.MAGENTA),
        POST_TELEPORT(Color.MAGENTA),
        UNKNOWN(Color.GRAY),
        PLUGIN(Color.CYAN);

        public final Color displayColour;

        TicketType(final Color displayColour) {
            this.displayColour=  displayColour;
        }

        public static TicketType getByString(final String ticketType) {
            return TicketType.valueOf(ticketType.toUpperCase(Locale.ENGLISH));
        }
    }
}