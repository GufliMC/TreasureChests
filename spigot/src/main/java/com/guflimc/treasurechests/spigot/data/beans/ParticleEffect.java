package com.guflimc.treasurechests.spigot.data.beans;

import com.guflimc.treasurechests.spigot.particle.AbstractParticleJob;
import com.guflimc.treasurechests.spigot.particle.RandomParticleJob;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ParticleEffect(ParticleType type, ParticlePattern pattern) {

    // TYPES

    public enum ParticleType {
//        ENDER("Ender", Material.ENDER_EYE, Particle.PORTAL),
//        ENCHANT("Enchantment", Material.ENCHANTED_BOOK, Particle.ENCHANTMENT_TABLE),
//        NOTE("Music", Material.MUSIC_DISC_STAL, Particle.NOTE),
//        HEART("Love", Material.APPLE, Particle.HEART),
        DUST_BLACK("Black dust", Material.BLACK_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(1908001), 1)),
        DUST_RED("Red dust", Material.RED_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(11546150), 1)),
        DUST_GREEN("Green dust", Material.GREEN_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(6192150), 1)),
        DUST_BROWN("Brown dust", Material.BROWN_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(8606770), 1)),
        DUST_BLUE("Blue dust", Material.BLUE_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(3949738), 1)),
        DUST_PURPLE("Purple dust", Material.PURPLE_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(8991416), 1)),
        DUST_CYAN("Cyan dust", Material.CYAN_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(1481884), 1)),
        DUST_LIGHT_GRAY("Light gray dust", Material.LIGHT_GRAY_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(10329495), 1)),
        DUST_GRAY("Gray dust", Material.GRAY_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(4673362), 1)),
        DUST_PINK("Pink dust", Material.PINK_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(15961002), 1)),
        DUST_LIME("Lime dust", Material.LIME_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(8439583), 1)),
        DUST_YELLOW("Yellow dust", Material.YELLOW_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(16701501), 1)),
        DUST_LIGHT_BLUE("Light blue dust", Material.LIGHT_BLUE_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(3847130), 1)),
        DUST_MAGENTA("Magenta dust", Material.MAGENTA_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(13061821), 1)),
        DUST_ORANGE("Orange dust", Material.ORANGE_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(16351261), 1)),
        DUST_WHITE("White dust", Material.WHITE_DYE, Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(16383998), 1)),
        ;
        private final String display;
        private final Material material;
        private final Particle particle;
        private final Object data;

        ParticleType(@NotNull String display, @NotNull Material material, @NotNull Particle particle, @Nullable Object data) {
            this.display = display;
            this.material = material;
            this.particle = particle;
            this.data = data;
        }

        ParticleType(String display, Material material, Particle particle) {
            this(display, material, particle, null);
        }

        public @NotNull String display() {
            return display;
        }

        public @NotNull Material material() {
            return material;
        }

        public @NotNull Particle particle() {
            return particle;
        }

        public @Nullable Object data() {
            return data;
        }
    }

    // PATTERN

    public enum ParticlePattern {
        RANDOM(RandomParticleJob::new),
        ;

        private final ParticlePatternCreator<?> creator;

        ParticlePattern(ParticlePatternCreator<?> creator) {
            this.creator = creator;
        }

        public ParticlePatternCreator<?> creator() {
            return creator;
        }
    }

    @FunctionalInterface
    public interface ParticlePatternCreator<T extends AbstractParticleJob> {
        T create(@NotNull JavaPlugin plugin, @NotNull Location location, @NotNull ParticleType particleType);
    }

}
