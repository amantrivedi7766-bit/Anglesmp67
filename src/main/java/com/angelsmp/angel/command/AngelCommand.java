package com.angelsmp.angel.command;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.gui.AdminGui;
import com.angelsmp.angel.gui.MenuGui;
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

/** The single {@code /angel} command with all of its subcommands. */
public class AngelCommand implements CommandExecutor, TabCompleter {

    private final AngelPlugin plugin;

    public AngelCommand(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "power", "p", "cast" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can cast Angel powers."));
                    return true;
                }
                if (!player.hasPermission("angel.use")) {
                    player.sendMessage(Text.color("&cYou lack permission to use Angel powers."));
                    return true;
                }
                plugin.getAbilityManager().activate(player, plugin.getPlayerManager().get(player.getUniqueId()));
                return true;
            }
            case "menu", "gui" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can open the Angel menu."));
                    return true;
                }
                new MenuGui(plugin, player).open();
                return true;
            }
            case "admin" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ You do not have access to the Admin Dashboard."));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can open the Admin Dashboard."));
                    return true;
                }
                new AdminGui(plugin, player).open();
                return true;
            }
            case "set" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ You do not have permission to set elements."));
                    return true;
                }
                return handleSet(sender, args);
            }
            case "reload" -> {
                if (!sender.hasPermission("angel.admin")) {
                    sender.sendMessage(Text.color("&c&l✦ You do not have permission to reload the plugin."));
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage(Text.color("&a&l✦ AngelSMP reloaded. &7config.yml refreshed."));
                return true;
            }
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            default -> {
                sender.sendMessage(Text.color("&cUnknown subcommand. Use &e/angel help&c."));
                return true;
            }
        }
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Text.color("&cUsage: /angel set <player> <element>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&c&l✦ Player not found: &f" + args[1]));
            return true;
        }
        AngelElement element = AngelElement.fromString(args[2]);
        if (element == null) {
            sender.sendMessage(Text.color("&c&l✦ Unknown element: &f" + args[2]
                    + "&c. Valid: fire, ice, lightning, wind, earth, light."));
            return true;
        }
        plugin.getPlayerManager().setElement(target.getUniqueId(), element);
        sender.sendMessage(Text.color("&a&l✦ " + target.getName() + " &7is now " + element.getColoredName() + "&7."));
        target.sendMessage(Text.color("&e&l✦ Your Angel element is now " + element.getColoredName() + "&e."));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.color("&6&l✦ AngelSMP Commands ✦"));
        sender.sendMessage(Text.color("&e/angel power &7- Activate your elemental ability."));
        sender.sendMessage(Text.color("&e/angel menu &7- Open the Angel interface."));
        if (sender.hasPermission("angel.admin")) {
            sender.sendMessage(Text.color("&e/angel admin &7- Open the Operator Admin Dashboard."));
            sender.sendMessage(Text.color("&e/angel set <player> <element> &7- Override a player's element."));
            sender.sendMessage(Text.color("&e/angel reload &7- Reload config.yml on the fly."));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"power", "menu", "gui", "admin", "set", "reload", "help"}) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                out.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            for (AngelElement element : AngelElement.values()) {
                out.add(element.name().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }
}
