package com.gufli.treasurechests.app.data.converters;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ItemStackConverter implements AttributeConverter<ItemStack, String> {

    // hehe
    @Override
    public String convertToDatabaseColumn(ItemStack attribute) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", attribute);
        return config.saveToString();
    }

    @Override
    public ItemStack convertToEntityAttribute(String dbData) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(dbData);
        } catch (Exception ignored) {
            return null;
        }
        return config.getItemStack("item", null);
    }
}