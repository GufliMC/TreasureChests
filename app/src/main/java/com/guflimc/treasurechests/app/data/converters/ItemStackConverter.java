package com.guflimc.treasurechests.app.data.converters;

import com.guflimc.treasurechests.app.util.BukkitSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
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