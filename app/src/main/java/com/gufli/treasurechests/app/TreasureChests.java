package com.gufli.treasurechests.app;

import co.aikar.commands.CommandManager;
import co.aikar.commands.CommandReplacements;
import co.aikar.commands.PaperCommandManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gufli.treasurechests.app.commands.RootCommand;
import com.gufli.treasurechests.app.data.DatabaseContext;
import com.gufli.treasurechests.app.listeners.PlayerChestListener;
import com.gufli.treasurechests.app.listeners.PlayerChestSetupListener;
import com.gufli.treasurechests.app.listeners.PlayerConnectionListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TreasureChests extends JavaPlugin {

    private DatabaseContext databaseContext;
    private TreasureChestManager treasureChestManager;

    //


    @Override
    public void onEnable() {
        getLogger().info("Enabling " + nameAndVersion() + ".");

        saveResource("config.json", false);

        // LOAD CONFIG
        JsonObject config;
        try (
                InputStream is = new File(getDataFolder(), "config.json").toURI().toURL().openStream();
                InputStreamReader isr = new InputStreamReader(is);
        ) {
            config = JsonParser.parseReader(isr).getAsJsonObject().get("database").getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // INIT DATABASE
        databaseContext = new DatabaseContext();
        try {
            databaseContext.withContextClassLoader(() -> {
                databaseContext.init(config);

                treasureChestManager = new TreasureChestManager(databaseContext);
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // commands
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        CommandReplacements replacements = commandManager.getCommandReplacements();
        replacements.addReplacement("root", "treasurechests|tc");

        commandManager.registerCommand(new RootCommand(treasureChestManager));

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
