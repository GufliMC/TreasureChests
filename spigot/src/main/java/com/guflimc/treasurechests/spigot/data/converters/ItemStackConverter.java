package com.guflimc.treasurechests.spigot.data.converters;

import com.guflimc.treasurechests.spigot.util.BukkitSerializer;
import org.bukkit.inventory.ItemStack;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class ItemStackConverter implements AttributeConverter<ItemStack, String> {

    @Override
    public String convertToDatabaseColumn(ItemStack attribute) {
        try {
            return BukkitSerializer.encodeObject(attribute);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ItemStack convertToEntityAttribute(String dbData) {
        try {
            return BukkitSerializer.decodeObject(ItemStack.class, dbData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}