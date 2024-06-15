package com.guflimc.treasurechests.spigot.placeholders;

import com.guflimc.brick.i18n.api.time.DurationFormatter;
import com.guflimc.brick.placeholders.api.module.PlaceholderModule;
import com.guflimc.brick.placeholders.api.module.PlaceholderTreeModule;
import com.guflimc.brick.placeholders.api.resolver.PlaceholderResolveContext;
import com.guflimc.treasurechests.spigot.TreasureChestManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TreasureChestCountdownPlaceholders extends PlaceholderTreeModule<Player> implements PlaceholderModule<Player> {

    private final TreasureChestManager manager;

    public TreasureChestCountdownPlaceholders(TreasureChestManager manager) {
        super("treasurechests");
        this.manager = manager;

        register("digital", (idp, ctx) -> format(idp, ctx, DurationFormatter.DIGITAL));
        register("cozy", (idp, ctx) -> format(idp, ctx, DurationFormatter.COZY));
    }

    public @Nullable Component format(@NotNull String idPrefix, @NotNull PlaceholderResolveContext<Player> placeholderResolveContext, @NotNull DurationFormatter formatter) {
        Duration duration = manager.chests().stream()
                .filter(chest -> chest.id().toString().startsWith(idPrefix))
                .findFirst()
                .map(chest -> manager.respawnIn(chest, placeholderResolveContext.entity()))
                .orElse(null);

        if ( duration == null ) {
            return null;
        }

        return Component.text(formatter.format(duration));
    }
}
