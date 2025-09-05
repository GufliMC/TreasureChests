package com.guflimc.treasurechests.spigot.placeholders;

import com.guflimc.brick.i18n.spigot.api.SpigotTranslator;
import com.guflimc.brick.placeholders.api.module.PlaceholderModule;
import com.guflimc.brick.placeholders.api.module.PlaceholderTreeModule;
import com.guflimc.treasurechests.spigot.TreasureChestManager;
import org.bukkit.entity.Player;

public class TreasureChestPlaceholders extends PlaceholderTreeModule<Player> implements PlaceholderModule<Player> {

    public TreasureChestPlaceholders(TreasureChestManager manager, SpigotTranslator translator) {
        super("treasurechests");
        register("countdown", new TreasureChestCountdownPlaceholders(manager, translator));
    }

}
