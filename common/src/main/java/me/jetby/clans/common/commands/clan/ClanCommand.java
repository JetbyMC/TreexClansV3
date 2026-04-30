package me.jetby.clans.common.commands.clan;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.service.clan.member.rank.RankPerms;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.Config;
import me.jetby.clans.common.gui.GuiFactory;
import me.jetby.clans.common.gui.GuiFactoryRequest;
import me.jetby.clans.common.gui.GuiLoader;
import me.jetby.clans.common.gui.GuiType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final TreexClans plugin;
    private final CommandService commandService;
    private final Map<String, List<String>> menuArgs = new HashMap<>();

    public ClanCommand(TreexClans plugin) {
        this.plugin = plugin;
        this.commandService = plugin.getCommandService();
        // TODO sex
        GuiLoader.ALL_GUIS.forEach((key, configuration) -> {
            menuArgs.put(key, configuration.getStringList("open_args"));
        });

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (args.length < 1) {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                    for (String str : plugin.getMessages().getConfig().getStringList("commands.help-no-clan")) {
                        sender.sendMessage(Config.CONFIG_COLORIZER.deserialize(str));
                    }
                } else {
                    for (String str : plugin.getMessages().getConfig().getStringList("commands.help")) {
                        sender.sendMessage(Config.CONFIG_COLORIZER.deserialize(str));
                    }
                }

                return true;
            }
            var apiArg = commandService.getCommands().get(args[0]);
            if (apiArg != null && apiArg.type() == CommandService.CommandType.CLAN) {
                apiArg.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                if (entry.getValue().contains(args[0])) {
                    FileConfiguration configuration = GuiLoader.ALL_GUIS.get(entry.getKey());
                    String listen = configuration.getString("listen", "default");
                    GuiType type = isBuiltInGuiType(listen);

                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                        if (type != GuiType.DEFAULT && type != GuiType.TOP_CLANS) {
                            return true;
                        }
                    }

                    GuiFactory.create(GuiFactoryRequest.builder()
                                    .player(player)
                                    .plugin(plugin)
                                    .configuration(configuration)
                                    .clan(clanImpl)
                                    .build())
                            .open(player);

                    return true;

                }
            }
        }

        if (args[0].equalsIgnoreCase("glow")) {
            if (!plugin.getModules().isGlow()) {
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("setslogan")) {
            if (!plugin.getModules().isSlogan()) {
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("setprefix")) {
            if (!plugin.getModules().isSetprefix()) {
                return true;
            }
        }
        try {
            var arg = ClanCommandArgs.valueOf(args[0].toUpperCase());
            arg.getSubcommand().onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown command. Use /" + command.getName() + " for help.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) return List.of();

            List<String> completions = Arrays.stream(ClanCommandArgs.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            completions.removeIf(cmd ->
                    switch (cmd) {
                        case "glow" -> !plugin.getModules().isGlow();
                        case "setslogan" -> !plugin.getModules().isSlogan();
                        default -> false;
                    });

            // todo sex
//            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
//                Menu menu = plugin.getGuiLoader().getGuis().get(entry.getKey());
//
//                if (isBuiltInGuiType(menu.type())) {
//                    if (GuiType.valueOf(menu.type()) == GuiType.DEFAULT) {
//                        if (player.hasPermission(menu.permission())) {
//                            completions.addAll(entry.getValue());
//                        }
//                    }
//                } else {
//                    if (player.hasPermission(menu.permission())) {
//                        completions.addAll(entry.getValue());
//                    }
//                }
//            }

            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                List<String> extra = completions.stream()
                        .filter(cmd -> cmd.equalsIgnoreCase("create") || cmd.equalsIgnoreCase("accept"))
                        .collect(Collectors.toList());

                for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                    FileConfiguration configuration = GuiLoader.ALL_GUIS.get(entry.getKey());
                    String listen = configuration.getString("listen");
                    GuiType type = isBuiltInGuiType(listen);

                    if ((type == GuiType.DEFAULT || type == GuiType.TOP_CLANS)
                        // todo OpenRequirements perm
//                                && player.hasPermission(menu.permission())
                    ){
                        extra.addAll(entry.getValue());
                    }

                }

                return extra.stream()
                        .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                        .toList();
            }

            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            var memberImpl = clanImpl.getMember(player.getUniqueId());

            if (memberImpl == null || memberImpl.getRank() == null)
                return List.of();

            var perms = memberImpl.getRank().perms();

            completions.removeIf(cmd -> switch (cmd) {
                case "setbase" -> !perms.contains(RankPerms.SETBASE);
                case "base" -> !perms.contains(RankPerms.BASE);
                case "invite" -> !perms.contains(RankPerms.INVITE);
                case "withdraw" -> !perms.contains(RankPerms.WITHDRAW) || plugin.getEconomy() == null;
                case "deposit", "invest" -> !perms.contains(RankPerms.DEPOSIT) || plugin.getEconomy() == null;
                case "kick" -> !perms.contains(RankPerms.KICK);
                case "pvp" -> !perms.contains(RankPerms.PVP);
                case "setslogan" -> !perms.contains(RankPerms.SETSLOGAN);
                case "setprefix" -> !perms.contains(RankPerms.SETPREFIX);
                default -> false;
            });
            completions.remove("create");
            completions.remove("accept");

            // todo sex
            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                FileConfiguration configuration = GuiLoader.ALL_GUIS.get(entry.getKey());
                String listen = configuration.getString("listen");
                GuiType type = isBuiltInGuiType(listen);

                // todo OpenRequirements perm
//                if (player.hasPermission(menu.permission())) {
                    completions.addAll(entry.getValue().stream()
                            .filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()))
                            .toList());
//                }
            }

            return completions.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        try {
            var arg = ClanCommandArgs.valueOf(args[0].toUpperCase());
            return arg.getSubcommand().onTabCompleter(sender, command, s, args);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private GuiType isBuiltInGuiType(String type) {
        try {
            return GuiType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}