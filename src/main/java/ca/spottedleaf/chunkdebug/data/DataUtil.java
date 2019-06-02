package ca.spottedleaf.chunkdebug.data;

public final class DataUtil {

    public static final int DATA_VERSION = 0;
    public static final int TICKET_LEVEL_UNLOAD_THRESHOLD = 45; // tickets at or above this level get unloaded

    public static long getCoordinateKey(final int x, final int z) {
        return (x & 0xFFFFFFFFL) | ((long)z << 32);
    }

    public static int getCoordinateX(final long key) {
        return (int)key;
    }

    public static int getCoordinateZ(final long key) {
        return (int)(key >>> 32);
    }

    public static int getResultingTicketLevel(final int startingLevel, final int startX, final int startZ, final int targetX,
                                              final int targetZ) {
        // In order to find a ticket level for a certain chunk, we need an origin ticket (startingLevel, startX, startZ)
        // Since the neighbours of that ticket have level starting + 1, and the neighbours of those have + 2, it becomes
        // a matter of expanding until we reach targetX.
        // The amount of times required to expand to get targetX on the border is simply the largest diff of the coordinates.

        final int diffX = Math.abs(targetX - startX);
        final int diffZ = Math.abs(targetZ - startZ);
        return startingLevel + Math.max(diffX, diffZ);
    }

    public static int getSquareRadiusForTicket(final int ticketLevel) {
        return (TICKET_LEVEL_UNLOAD_THRESHOLD - ticketLevel - 1);
    }

    public static int getTotalChunksForRadius(final int squareRadius) {
        return (2 * squareRadius + 1) * (2 * squareRadius + 1);
    }

    public static int getTotalChunksForTicket(final int ticketLevel) {
        return getTotalChunksForRadius(getSquareRadiusForTicket(ticketLevel));
    }

    private DataUtil() {
        throw new RuntimeException();
    }
}