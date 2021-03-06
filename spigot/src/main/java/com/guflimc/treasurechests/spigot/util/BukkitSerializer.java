package com.guflimc.treasurechests.spigot.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Base64;

/**
 * A class that serializes and deserializes {@link ConfigurationSerializable} objects.
 */
public class BukkitSerializer {


    public static <T extends ConfigurationSerializable> String encodeObject(T object) throws IOException {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        ) {
            dataOutput.writeObject(object);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static <T extends ConfigurationSerializable> T decodeObject(Class<T> type, String base64) throws IOException, ClassNotFoundException {
        base64 = base64.replace(System.getProperty("line.separator"), "");  // legacy convert
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ) {
            T object = (T) dataInput.readObject();
            dataInput.close();
            return object;
        }
    }

    public static <T extends ConfigurationSerializable> String encodeArray(T[] array) throws IOException {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        ) {
            dataOutput.writeInt(array.length);
            for (T t : array) {
                dataOutput.writeObject(t);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static <T extends ConfigurationSerializable> T[] decodeArray(Class<T> type, String base64) throws IOException, ClassNotFoundException {
        base64 = base64.replace(System.getProperty("line.separator"), ""); // legacy convert
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ) {
            T[] items = (T[]) Array.newInstance(type, dataInput.readInt());
            for (int i = 0; i < items.length; i++) {
                items[i] = (T) dataInput.readObject();
            }
            return items;
        }
    }

}
