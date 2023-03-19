package com.guflimc.treasurechests.spigot;

import com.gufli.dbeantools.api.DatabaseConfig;

public class TreasureChestsConfig {

    public DatabaseConfig database = new DatabaseConfig();

    public TreasureChestsConfig() {
        database.dsn = "jdbc:h2:file:./plugins/TreasureChests/data/database.h2";
        database.username = "dbuser";
        database.password = "dbuser";
    }

}
