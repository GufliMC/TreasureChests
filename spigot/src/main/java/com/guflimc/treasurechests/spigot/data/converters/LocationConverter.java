package com.guflimc.treasurechests.spigot.data.converters;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class LocationConverter implements AttributeConverter<Location, String> {

    private final static JsonDeserializer<Location> deserializer = (json, typeOfT, context) -> {
        JsonObject obj = json.getAsJsonObject();
        return new Location(
                Bukkit.getWorld(UUID.fromString(obj.get("world").getAsString())),
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble()
        );
    };

    private final static JsonSerializer<Location> serializer = (src, typeOfSrc, context) -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("world", src.getWorld().getUID().toString());
        obj.addProperty("x", src.getX());
        obj.addProperty("y", src.getY());
        obj.addProperty("z", src.getZ());
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