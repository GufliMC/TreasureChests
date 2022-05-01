package com.gufli.treasurechests.app.data.converters;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class InventoryConverter implements AttributeConverter<Inventory, String> {

    @Override
    public String convertToDatabaseColumn(Inventory attribute) {
        YamlConfiguration config = new YamlConfiguration();
        for ( int i = 0; i < attribute.getSize(); i++) {
            if ( attribute.getItem(i) == null ) continue;
            config.set(i + "", attribute.getItem(i));
        }
        return config.saveToString();
    }

    @Override
    public Inventory convertToEntityAttribute(String dbData) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(dbData);
        } catch (Exception ignored) {
            return null;
        }

        int size = Math.max(9, (int) Math.ceil(config.getKeys(false).size() / 9.0) * 9);

        Inventory inv = Bukkit.createInventory(null, size);
        for ( String key : config.getKeys(false) ) {
            inv.setItem(Integer.parseInt(key), config.getItemStack(key));
        }
        return inv;
    }
}