package com.guflimc.treasurechests.spigot.placeholders;

import com.guflimc.brick.placeholders.api.module.PlaceholderModule;
import com.guflimc.brick.placeholders.api.module.PlaceholderTreeModule;
import com.guflimc.treasurechests.spigot.TreasureChestManager;
import org.bukkit.entity.Player;

public class TreasureChestPlaceholders extends PlaceholderTreeModule<Player> implements PlaceholderModule<Player> {

    public TreasureChestPlaceholders(TreasureChestManager manager) {
        super("treasurechests");
        register("countdown", new TreasureChestCountdownPlaceholders(manager));
    }

}
