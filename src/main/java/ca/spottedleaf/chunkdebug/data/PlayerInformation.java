package ca.spottedleaf.chunkdebug.data;

import com.google.gson.JsonObject;

public final class PlayerInformation implements Comparable<PlayerInformation> {

    public final String name;
    public final double x;
    public final double y;
    public final double z;

    public PlayerInformation(final String name, final double x, final double y, final double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compareTo(final PlayerInformation other) {
        return this.name.compareTo(other.name);
    }

    public static PlayerInformation fromJson(final JsonObject object) {
        final String name = object.get("name").getAsString();
        final double x = object.get("x").getAsDouble();
        final double y = object.get("y").getAsDouble();
        final double z = object.get("z").getAsDouble();

        return new PlayerInformation(name, x, y, z);
    }

}