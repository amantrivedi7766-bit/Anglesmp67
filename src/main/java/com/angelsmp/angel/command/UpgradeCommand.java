package com.angelsmp.angel.command;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.gui.UpgradeGui;
import com.angelsmp.angel.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Module 4B Path B - the Sacrifice Upgrade GUI ({@code /upgrade}). */
public class UpgradeCommand implements CommandExecutor {

    private final AngelPlugin plugin;

    public UpgradeCommand(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can open the upgrade menu."));
            return true;
        }
        new UpgradeGui(plugin, player).open();
        return true;
    }
}
