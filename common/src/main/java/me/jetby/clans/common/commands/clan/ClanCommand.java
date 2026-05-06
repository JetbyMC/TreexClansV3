package me.jetby.clans.common.commands.clan;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
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
        GuiLoader.CUSTOM_GUIS.forEach((key, configuration) -> {
            menuArgs.put(key, configuration.getStringList("open_args"));
        });
        GuiLoader.REQUIRED_GUIS.forEach((key, configuration) -> {
            menuArgs.put(key.name(), configuration.getStringList("open_args"));
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
            Subcommand sub = commandService.getCommands().get(args[0]);
            if (sub != null && sub.type() == CommandService.CommandType.CLAN) {
                sub.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                if (entry.getValue().contains(args[0])) {
                    FileConfiguration configuration = GuiLoader.getGuiConfiguration(entry.getKey());
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
                                    .clan(clan)
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
            ClanCommandArgs arg = ClanCommandArgs.valueOf(args[0].toUpperCase());
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
                    FileConfiguration configuration = GuiLoader.getGuiConfiguration(entry.getKey());
                    String listen = configuration.getString("listen");
                    GuiType type = isBuiltInGuiType(listen);

                    if ((type == GuiType.DEFAULT || type == GuiType.TOP_CLANS)
                        // todo OpenRequirements perm
//                                && player.hasPermission(menu.permission())
                    ) {
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
                case "setbase" -> !perms.contains(RankPerm.SETBASE);
                case "base" -> !perms.contains(RankPerm.BASE);
                case "invite" -> !perms.contains(RankPerm.INVITE);
                case "withdraw" -> !perms.contains(RankPerm.WITHDRAW);
                case "deposit", "invest" -> !perms.contains(RankPerm.DEPOSIT);
                case "kick" -> !perms.contains(RankPerm.KICK);
                case "pvp" -> !perms.contains(RankPerm.PVP);
                case "setslogan" -> !perms.contains(RankPerm.SETSLOGAN);
                case "setprefix" -> !perms.contains(RankPerm.SETPREFIX);
                default -> false;
            });
            completions.remove("create");
            completions.remove("accept");

            // todo sex
            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                FileConfiguration configuration = GuiLoader.getGuiConfiguration(entry.getKey());
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