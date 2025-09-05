package com.guflimc.treasurechests.spigot;

import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.brick.i18n.spigot.api.SpigotTranslator;
import com.guflimc.brick.placeholders.spigot.api.manager.SpigotPlaceholderManager;
import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestSetupListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerConnectionListener;
import com.guflimc.treasurechests.spigot.listeners.WorldListener;
import com.guflimc.treasurechests.spigot.particle.ParticleJobManager;
import com.guflimc.treasurechests.spigot.placeholders.TreasureChestPlaceholders;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class TreasureChests extends JavaPlugin {

    private DatabaseContext databaseContext;
    private TreasureChestManager manager;
    private ParticleJobManager particleJobManager;

    private SpigotTranslator translator;
    //

    @Override
    public void onEnable() {
        // LOAD CONFIG
        TreasureChestsConfig config = new TreasureChestsConfig();

        // translations
        translator = new SpigotTranslator(this, Locale.ENGLISH);
        translator.importTranslations(this);

        // INIT DATABASE
        databaseContext = new DatabaseContext(config.database);

        // manager
        manager = new TreasureChestManager(this, databaseContext);

        // particle
        particleJobManager = new ParticleJobManager(this, manager);

        // placeholders
        SpigotPlaceholderManager placeholderManager = new SpigotPlaceholderManager(this);
        placeholderManager.register(new TreasureChestPlaceholders(manager, translator));

        // init guis
        SpigotBrickGUI.register(this);

        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(manager), this);
        pm.registerEvents(new PlayerChestListener(manager, translator), this);
        pm.registerEvents(new PlayerChestSetupListener(manager, particleJobManager), this);
        pm.registerEvents(new WorldListener(particleJobManager), this);

        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void onDisable() {
        // DATABASE
        if (databaseContext != null) {
            databaseContext.shutdown();
        }

        if (manager != null) {
            manager.shutdown();
        }

        if ( particleJobManager != null ) {
            particleJobManager.shutdown();
        }

        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getDescription().getName() + " v" + getDescription().getVersion();
    }

}
