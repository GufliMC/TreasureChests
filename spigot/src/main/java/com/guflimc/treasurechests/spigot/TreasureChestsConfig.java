package com.guflimc.treasurechests.spigot;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;

public class TreasureChestsConfig {

    public EbeanConfig database = new EbeanConfig();

    public TreasureChestsConfig() {
        database.dsn = "jdbc:h2:file:./plugins/TreasureChests/data/database.h2";
        database.driver = "org.h2.Driver";
        database.username = "dbuser";
        database.password = "dbuser";
    }

}
