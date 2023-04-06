package com.guflimc.treasurechests.spigot.data;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;
import com.guflimc.brick.orm.ebean.database.EbeanDatabaseContext;
import com.guflimc.brick.orm.ebean.database.EbeanMigrations;
import com.guflimc.brick.orm.jpa.converters.ComponentConverter;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChest;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChestInventory;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureLoot;
import com.guflimc.treasurechests.spigot.data.converters.InventoryConverter;
import com.guflimc.treasurechests.spigot.data.converters.ItemStackConverter;
import com.guflimc.treasurechests.spigot.data.converters.LocationConverter;
import io.ebean.annotation.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

public class DatabaseContext extends EbeanDatabaseContext {

    public final static String DATASOURCE_NAME = "TreasureChests";

    public DatabaseContext(EbeanConfig config) {
        super(config, DATASOURCE_NAME);
    }

    @Override
    protected Class<?>[] applicableClasses() {
        return APPLICABLE_CLASSES;
    }

    private static final Class<?>[] APPLICABLE_CLASSES = new Class[] {
            ItemStackConverter.class,
            LocationConverter.class,
            InventoryConverter.class,
            ComponentConverter.class,

            BTreasureChest.class,
            BTreasureLoot.class,
            BTreasureChestInventory.class
    };

    public static void main(String[] args) throws IOException, SQLException {
        EbeanMigrations generator = new EbeanMigrations(
                DATASOURCE_NAME,
                Path.of("TreasureChests/spigot/src/main/resources"),
                Platform.H2
        );
        Arrays.stream(APPLICABLE_CLASSES).forEach(generator::addClass);
        generator.generate();
    }
}
