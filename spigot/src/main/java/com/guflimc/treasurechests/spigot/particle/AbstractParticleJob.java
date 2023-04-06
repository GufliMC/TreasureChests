package com.guflimc.treasurechests.spigot.particle;

import com.guflimc.treasurechests.spigot.data.beans.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class AbstractParticleJob {

    protected final JavaPlugin plugin;
    protected final Location location;
    protected final ParticleEffect.ParticleType particleType;
    protected final int interval;

    //

    private boolean running = false;
    private BukkitTask task;

    public AbstractParticleJob(JavaPlugin plugin, Location location, ParticleEffect.ParticleType particleType, int interval) {
        this.plugin = plugin;
        this.location = location;
        this.particleType = particleType;
        this.interval = interval;
    }

    public final void start() {
        if ( running ) return;
        running = true;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 0L, interval);
    }

    public final void stop() {
        if ( !running ) return;
        running = false;
        task.cancel();
    }

    protected abstract void onTick();

}
