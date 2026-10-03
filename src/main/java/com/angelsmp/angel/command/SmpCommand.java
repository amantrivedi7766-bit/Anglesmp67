package com.angelsmp.angel.command;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.gui.AdminMenuGui;
import com.angelsmp.angel.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/** Module 5B - the Master Admin Overlord Menu ({@code /smp menu}). */
public class SmpCommand implements CommandExecutor {

    private final AngelPlugin plugin;

    public SmpCommand(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("angel.admin")) {
            sender.sendMessage(Text.color("&c&l✦ You do not have access to the Admin Overlord Menu."));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can open the Admin Overlord Menu."));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            new AdminMenuGui(plugin, player).open();
            return true;
        }
        sender.sendMessage(Text.color("&cUsage: /smp menu"));
        return true;
    }
}
