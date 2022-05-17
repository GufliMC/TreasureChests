package com.guflimc.treasurechests.spigot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestSetupListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerConnectionListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TreasureChests extends JavaPlugin {

    private static final Gson gson = new Gson();

    private DatabaseContext databaseContext;
    private TreasureChestManager treasureChestManager;

    //

    @Override
    public void onEnable() {
        saveResource("config.json", false);

        // LOAD CONFIG
        TreasureChestsConfig config;
        try (
                InputStream is = getResource("config.json");
                InputStreamReader isr = new InputStreamReader(is)
        ) {
            config = gson.fromJson(isr, TreasureChestsConfig.class);
        } catch (IOException e) {
            getLogger().severe("Cannot load configuration.");
            e.printStackTrace();
            return;
        }

        // INIT DATABASE
        databaseContext = new DatabaseContext();
        try {
            databaseContext.withContextClassLoader(() -> {
                databaseContext.init(config.database);
                databaseContext.migrate();

                treasureChestManager = new TreasureChestManager(this, databaseContext);
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // init guis
        SpigotBrickGUI.register(this);

        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(treasureChestManager), this);
        pm.registerEvents(new PlayerChestListener(treasureChestManager), this);
        pm.registerEvents(new PlayerChestSetupListener(treasureChestManager), this);

        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void onDisable() {
        // DATABASE
        if (databaseContext != null) {
            databaseContext.shutdown();
        }

        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getDescription().getName() + " v" + getDescription().getVersion();
    }

}
