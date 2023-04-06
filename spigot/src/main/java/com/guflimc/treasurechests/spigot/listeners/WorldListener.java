package com.guflimc.treasurechests.spigot.listeners;

import com.guflimc.treasurechests.spigot.particle.ParticleJobManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListener implements Listener {

    private final ParticleJobManager particleJobManager;

    public WorldListener(ParticleJobManager particleJobManager) {
        this.particleJobManager = particleJobManager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        particleJobManager.start(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        particleJobManager.stop(event.getChunk());
    }
}
