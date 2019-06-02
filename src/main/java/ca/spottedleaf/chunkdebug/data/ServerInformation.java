package ca.spottedleaf.chunkdebug.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerInformation {

    // serialized data
    public final String serverVersion;
    public final int dataVersion;
    public final List<WorldInformation> worlds;

    // interpreted data
    public final Map<String, WorldInformation> worldsByName;

    private ServerInformation(final String serverVersion, final int dataVersion, final List<WorldInformation> worlds,
                              final Map<String, WorldInformation> worldsByName) {
        this.serverVersion = serverVersion;
        this.dataVersion = dataVersion;
        this.worlds = worlds;
        this.worldsByName = worldsByName;
    }

    public static ServerInformation fromJson(final JsonObject object) {
        final int dataVersion = object.get("data-version").getAsInt();

        if (dataVersion != DataUtil.DATA_VERSION) {
            throw new IllegalArgumentException("Incompatible data versions! We are data version " + DataUtil.DATA_VERSION + ", data is " + dataVersion);
        }

        final String serverVersion = object.get("server-version").getAsString();

        final JsonArray worldsData = object.get("worlds").getAsJsonArray();
        final List<WorldInformation> worlds = new ArrayList<>(worldsData.size());
        final Map<String, WorldInformation> worldsByName = new HashMap<>(32);

        for (final JsonElement worldData : worldsData) {
            final WorldInformation world = WorldInformation.loadFromJson(worldData.getAsJsonObject());
            if (worldsByName.putIfAbsent(world.name, world) != null) {
                throw new IllegalArgumentException("Contains duplicate world " + world.name);
            }
            worlds.add(world);
        }

        return new ServerInformation(serverVersion, dataVersion, worlds, worldsByName);
    }

}