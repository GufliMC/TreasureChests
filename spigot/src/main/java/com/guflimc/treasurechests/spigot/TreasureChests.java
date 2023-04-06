package com.guflimc.treasurechests.spigot;

import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerChestSetupListener;
import com.guflimc.treasurechests.spigot.listeners.PlayerConnectionListener;
import com.guflimc.treasurechests.spigot.listeners.WorldListener;
import com.guflimc.treasurechests.spigot.particle.ParticleJobManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TreasureChests extends JavaPlugin {

    private DatabaseContext databaseContext;
    private TreasureChestManager manager;
    private ParticleJobManager particleJobManager;

    //

    @Override
    public void onEnable() {
        // LOAD CONFIG
        TreasureChestsConfig config = new TreasureChestsConfig();

        // INIT DATABASE
        databaseContext = new DatabaseContext(config.database);

        // manager
        manager = new TreasureChestManager(this, databaseContext);

        // particle
        particleJobManager = new ParticleJobManager(this, manager);

        // init guis
        SpigotBrickGUI.register(this);

        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(manager), this);
        pm.registerEvents(new PlayerChestListener(manager), this);
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
