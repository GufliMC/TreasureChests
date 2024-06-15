package com.guflimc.treasurechests.spigot;

import com.guflimc.brick.i18n.api.time.DurationFormatter;
import com.guflimc.brick.placeholders.api.module.PlaceholderModule;
import com.guflimc.brick.placeholders.api.resolver.PlaceholderResolveContext;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreasureChestPlaceholders implements PlaceholderModule<Player> {

    private final TreasureChestManager manager;

    public TreasureChestPlaceholders(TreasureChestManager manager) {
        this.manager = manager;
    }

    @Override
    public @NotNull String name() {
        return "treasurechests";
    }

    @Override
    public @Nullable Component resolve(@NotNull String s, @NotNull PlaceholderResolveContext<Player> placeholderResolveContext) {
        if ( !s.startsWith("countdown") ) {
            return null;
        }
        String idPrefix = s.substring("countdown_".length());
        return manager.chests().stream()
                .filter(chest -> chest.id().toString().startsWith(idPrefix))
                .findFirst()
                .map(chest -> manager.respawnIn(chest, placeholderResolveContext.entity()))
                .map(duration -> Component.text(DurationFormatter.DIGITAL.format(duration)))
                .orElse(null);
    }
}
