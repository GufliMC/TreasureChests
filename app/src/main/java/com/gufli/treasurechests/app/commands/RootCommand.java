package com.gufli.treasurechests.app.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.gufli.treasurechests.app.TreasureChestManager;
import com.gufli.treasurechests.app.session.PlayerSession;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%root")
public class RootCommand extends BaseCommand {

    private final TreasureChestManager manager;

    public RootCommand(TreasureChestManager manager) {
        this.manager = manager;
    }

    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @CommandPermission("treasurechests.create")
    public void create(Player player, boolean global, int respawnMinutes) {
        manager.setSession(player, new PlayerSession(global, respawnMinutes));
        player.sendMessage(ChatColor.GREEN + "Left-Click a chest to turn it into a treasure chest.");
    }

    @Subcommand("addloot")
    @CommandPermission("treasurechests.addloot")
    public void addLoot(Player player, double chance) {
        if ( chance < 0 || chance > 100 ) {
            player.sendMessage(ChatColor.RED + "Chance must be between 0 and 100.");
            return;
        }

        manager.setSession(player, new PlayerSession(chance / 100));
        player.sendMessage(ChatColor.GREEN + "Left-Click a treasure chest to add your hand to it");
    }

    @Subcommand("inspect")
    @CommandPermission("treasurechests.inspect")
    public void inspect(Player player) {
        manager.setSession(player, new PlayerSession(PlayerSession.PlayerSessionType.INSPECT));
        player.sendMessage(ChatColor.GREEN + "Left-Click a chest to inspect it.");
    }

}
