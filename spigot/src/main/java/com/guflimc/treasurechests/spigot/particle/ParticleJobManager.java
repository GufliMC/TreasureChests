package com.guflimc.treasurechests.spigot.particle;

import com.guflimc.treasurechests.spigot.TreasureChestManager;
import com.guflimc.treasurechests.spigot.TreasureChests;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChest;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleJobManager {

    private final TreasureChests plugin;
    private final TreasureChestManager manager;
    private final Map<BTreasureChest, AbstractParticleJob> jobs = new ConcurrentHashMap<>();

    public ParticleJobManager(TreasureChests plugin, TreasureChestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void shutdown() {
        jobs.values().forEach(AbstractParticleJob::stop);
        jobs.clear();
    }

    public void start(BTreasureChest chest) {
        stop(chest);

        if ( chest.particleEffect() == null || jobs.containsKey(chest) ) {
            return;
        }

        AbstractParticleJob job = chest.particleEffect().pattern().creator()
                .create(plugin, chest.bukkitLocation(), chest.particleEffect().type());
        job.start();
        jobs.put(chest, job);
    }

    public void start(Chunk chunk) {
        manager.chests().stream()
                .filter(chest -> chunk.getWorld().equals(chest.bukkitLocation().getWorld()))
                .filter(chest -> chunk.getX() == chest.location().blockX() >> 4)
                .filter(chest -> chunk.getZ() == chest.location().blockZ() >> 4)
                .forEach(this::start);
    }

    public void stop(BTreasureChest chest) {
        if ( !jobs.containsKey(chest) ) {
            return;
        }
        jobs.remove(chest).stop();
    }

    public void stop(Chunk chunk) {
        new HashSet<>(jobs.keySet()).stream()
                .filter(chest -> chunk.getWorld().equals(chest.bukkitLocation().getWorld()))
                .filter(chest -> chunk.getX() == chest.location().blockX() >> 4)
                .filter(chest -> chunk.getZ() == chest.location().blockZ() >> 4)
                .forEach(this::stop);
    }
}
