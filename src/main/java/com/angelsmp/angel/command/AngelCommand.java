package com.angelsmp.angel.command;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.gui.AdminGui;
import com.angelsmp.angel.gui.PlayerStatusGui;
import com.angelsmp.angel.gui.UpgradeGui;
import com.angelsmp.angel.item.AngelToken;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The single {@code /angel} command (plus {@code /upgrade}).
 * Subcommands: power, admin, upgrade, cast, token, reload.
 */
public class AngelCommand implements CommandExecutor, TabCompleter {

    private final AngelPlugin plugin;

    public AngelCommand(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                new PlayerStatusGui(plugin, player).open();
            } else {
                sender.sendMessage(Text.color("&cUsage: /angel <power|admin|upgrade|cast|token|reload>"));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "power" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can open this menu."));
                    return true;
                }
                new PlayerStatusGui(plugin, player).open();
            }
            case "upgrade" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can open this menu."));
                    return true;
                }
                new UpgradeGui(plugin, player).open();
            }
            case "cast" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can cast abilities."));
                    return true;
                }
                PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
                plugin.getAbilityManager().useActive(player, profile);
            }
            case "admin" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ You do not have access to the Admin Management GUI."));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can open the admin menu."));
                    return true;
                }
                new AdminGui(plugin, player).open();
            }
            case "token" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ No permission."));
                    return true;
                }
                return handleToken(sender, args);
            }
            case "reload" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ No permission."));
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage(Text.color("&a&l✦ AngelSMP reloaded."));
            }
            default -> sender.sendMessage(Text.color("&cUnknown subcommand. Use &e/angel power&c."));
        }
        return true;
    }

    private boolean handleToken(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Text.color("&cUsage: /angel token give <player> <amount>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(Text.color("&c&l✦ Player not found: &f" + args[2]));
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Text.color("&c&l✦ Invalid amount."));
            return true;
        }
        for (int i = 0; i < amount; i++) {
            target.getInventory().addItem(AngelToken.create(plugin));
        }
        sender.sendMessage(Text.color("&a&l✦ Gave " + amount + " Angel Tokens to " + target.getName() + "."));
        target.sendMessage(Text.color("&6&l✦ You received " + amount + " Angel Tokens!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"power", "admin", "upgrade", "cast", "token", "reload"}) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("token")) {
            out.add("give");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("token")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                out.add(player.getName());
            }
        }
        return out;
    }
}
