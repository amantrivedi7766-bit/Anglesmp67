package com.angelsmp.angel.command;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.gui.ElementSelectionGui;
import com.angelsmp.angel.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Module 5A - opens the Player Selection Screen ({@code /sparkgui} or {@code /angel power}). */
public class SparkGuiCommand implements CommandExecutor {

    private final AngelPlugin plugin;

    public SparkGuiCommand(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can open the element menu."));
            return true;
        }
        if (!player.hasPermission("angel.use")) {
            player.sendMessage(Text.color("&cYou lack permission to use the element menu."));
            return true;
        }
        new ElementSelectionGui(plugin, player).open();
        return true;
    }
}
