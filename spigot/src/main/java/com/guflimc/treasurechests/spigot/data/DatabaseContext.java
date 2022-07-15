package com.guflimc.treasurechests.spigot.data;

import com.gufli.dbeantools.adventure.converters.ComponentConverter;
import com.gufli.dbeantools.api.context.AbstractDatabaseContext;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChest;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChestInventory;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureLoot;
import com.guflimc.treasurechests.spigot.data.converters.InventoryConverter;
import com.guflimc.treasurechests.spigot.data.converters.ItemStackConverter;
import com.guflimc.treasurechests.spigot.data.converters.LocationConverter;

public class DatabaseContext extends AbstractDatabaseContext {

    public final static String DATASOURCE_NAME = "TreasureChests";

    public DatabaseContext() {
        super(DATASOURCE_NAME);
    }

    public Class<?>[] classes() {
        return new Class[]{
                ItemStackConverter.class,
                LocationConverter.class,
                InventoryConverter.class,
                ComponentConverter.class,

                BTreasureChest.class,
                BTreasureLoot.class,
                BTreasureChestInventory.class
        };
    }

}
