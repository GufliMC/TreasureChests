package com.guflimc.treasurechests.spigot.data.converters;

import com.google.gson.*;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import com.guflimc.brick.math.common.geometry.pos3.Vector3;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = true)
public class LocationConverter implements AttributeConverter<Location, String> {

    private final static JsonDeserializer<Location> deserializer = (json, typeOfT, context) -> {
        JsonObject obj = json.getAsJsonObject();
        return new Location(
                UUID.fromString(obj.get("world").getAsString()),
                new Vector3(
                        obj.get("x").getAsDouble(),
                        obj.get("y").getAsDouble(),
                        obj.get("z").getAsDouble()
                )
        );
    };

    private final static JsonSerializer<Location> serializer = (src, typeOfSrc, context) -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("world", src.worldId().toString());
        obj.addProperty("x", src.x());
        obj.addProperty("y", src.y());
        obj.addProperty("z", src.z());
        return obj;
    };

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Location.class, deserializer)
            .registerTypeAdapter(Location.class, serializer)
            .create();

    @Override
    public String convertToDatabaseColumn(Location attribute) {
        return gson.toJson(attribute);
    }

    @Override
    public Location convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData, Location.class);
    }
}