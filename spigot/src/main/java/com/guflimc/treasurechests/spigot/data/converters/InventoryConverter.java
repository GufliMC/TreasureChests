package com.guflimc.treasurechests.spigot.data.converters;

import com.guflimc.treasurechests.spigot.util.BukkitSerializer;
import com.guflimc.treasurechests.spigot.util.DatabaseWrapper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class InventoryConverter implements AttributeConverter<DatabaseWrapper<Inventory>, String> {

    @Override
    public String convertToDatabaseColumn(DatabaseWrapper<Inventory> attribute) {
        try {
            return BukkitSerializer.encodeArray(attribute.value.getContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DatabaseWrapper<Inventory> convertToEntityAttribute(String dbData) {
        try {
            ItemStack[] items = BukkitSerializer.decodeArray(ItemStack.class, dbData);
            Inventory inv = Bukkit.createInventory(null, items.length);
            for ( int i = 0; i < items.length; i++ ) {
                inv.setItem(i, items[i]);
            }
            return new DatabaseWrapper<>(inv);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}