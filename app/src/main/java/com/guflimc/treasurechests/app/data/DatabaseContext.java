package com.guflimc.treasurechests.app.data;

import com.guflimc.treasurechests.app.data.beans.BTreasureChest;
import com.guflimc.treasurechests.app.data.beans.BTreasureChestInventory;
import com.guflimc.treasurechests.app.data.beans.BTreasureLoot;
import com.guflimc.treasurechests.app.data.converters.InventoryConverter;
import com.guflimc.treasurechests.app.data.converters.ItemStackConverter;
import com.guflimc.treasurechests.app.data.converters.LocationConverter;
import io.ebean.config.DatabaseConfig;
import org.minestombrick.ebean.context.AbstractDatabaseContext;

import java.util.Collection;
import java.util.Set;

public class DatabaseContext extends AbstractDatabaseContext {

    public final static String DATASOURCE_NAME = "TreasureChests";

    public DatabaseContext() {
        super(DATASOURCE_NAME);
    }

    @Override
    protected void buildConfig(DatabaseConfig config) {
        classes().forEach(config::addClass);
    }

    public static Collection<Class<?>> classes() {
        return Set.of(
                ItemStackConverter.class,
                LocationConverter.class,
                InventoryConverter.class,

                BTreasureChest.class,
                BTreasureLoot.class,
                BTreasureChestInventory.class
        );
    }

}
