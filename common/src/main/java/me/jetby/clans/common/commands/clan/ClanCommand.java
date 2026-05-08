package me.jetby.clans.common.commands.clan;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.gui.ExtendedGui;
import me.jetby.clans.api.gui.GuiContext;
import me.jetby.clans.api.gui.ListenType;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.CommandsConfiguration;
import me.jetby.clans.common.configurations.Config;
import me.jetby.clans.common.gui.GuiLoader;
import me.jetby.libb.action.ActionUtil;
import me.jetby.libb.command.AdvancedCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClanCommand extends AdvancedCommand {

    private final TreexClans plugin;
    private final CommandService commandService;
    private final Map<String, List<String>> menuArgs = new HashMap<>();

    public ClanCommand(@NotNull CommandsConfiguration configuration, @NotNull TreexClans plugin) {
        super(configuration.getCommands().get(0), plugin);
        this.plugin = plugin;
        this.commandService = plugin.getCommandService();

        List<String> aliases = new ArrayList<>(configuration.getCommands());
        aliases.remove(0);
        getAliases().addAll(aliases);

        GuiLoader.CUSTOM_GUIS.forEach((key, gui) -> menuArgs.put(key, gui.getArgs()));
        GuiLoader.REQUIRED_GUIS.forEach((key, gui) -> menuArgs.put(key.name(), gui.getArgs()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0];
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        Subcommand registeredSub = commandService.getCommands().get(sub);
        if (registeredSub != null && registeredSub.type() == CommandService.CommandType.CLAN) {
            registeredSub.onCommand(sender, subArgs);
            return true;
        }

        if (tryOpenGui(player, sub)) return true;

        if (sub.equalsIgnoreCase("setslogan") && !plugin.getModules().isSlogan()) return true;
        if (sub.equalsIgnoreCase("setprefix") && !plugin.getModules().isSetprefix()) return true;

        try {
            ClanSubcommand.valueOf(sub.toUpperCase()).getSubcommand().onCommand(sender, subArgs);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown command. Use /" + command.getName() + " for help.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            return getFirstArgCompletions(player, args[0]);
        }

        try {
            ClanSubcommand sub = ClanSubcommand.valueOf(args[0].toUpperCase());
            return sub.getSubcommand().onTabCompleter(sender, command, s, args);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private void sendHelp(Player player) {
        String path = plugin.getClanManager().lookup().isInClan(player.getUniqueId())
                ? "commands.help"
                : "commands.help-no-clan";
        plugin.getMessages().getConfig().getStringList(path)
                .forEach(str -> player.sendMessage(Config.CONFIG_COLORIZER.deserialize(str)));
    }

    private boolean tryOpenGui(Player player, String arg) {
        for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
            if (!entry.getValue().contains(arg)) continue;

            ExtendedGui gui = GuiLoader.getGuiConfiguration(entry.getKey());
            if (gui == null) return false;

            boolean inClan = plugin.getClanManager().lookup().isInClan(player.getUniqueId());
            if (!inClan && gui.getListenType() != ListenType.DEFAULT && gui.getListenType() != ListenType.TOP_CLANS) {
                return true;
            }

            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            plugin.getGuiFactory().create(GuiContext.of(plugin, gui, player, clan)).open(player);
            return true;
        }
        return false;
    }

    private List<String> getFirstArgCompletions(Player player, String input) {
        boolean inClan = plugin.getClanManager().lookup().isInClan(player.getUniqueId());

        if (!inClan) {
            return getCompletionsForNonMember(player, input);
        }

        return getCompletionsForMember(player, input);
    }

    private List<String> getCompletionsForNonMember(Player player, String input) {
        List<String> completions = new ArrayList<>();

        Arrays.stream(ClanSubcommand.values())
                .map(e -> e.name().toLowerCase())
                .filter(cmd -> cmd.equals("create") || cmd.equals("accept"))
                .forEach(completions::add);

        for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
            ExtendedGui gui = GuiLoader.getGuiConfiguration(entry.getKey());
            if (gui == null) continue;
            if ((gui.getListenType() == ListenType.DEFAULT || gui.getListenType() == ListenType.TOP_CLANS)) {
                completions.addAll(entry.getValue());
            }
        }

        return completions.stream()
                .filter(cmd -> cmd.startsWith(input.toLowerCase()))
                .toList();
    }

    private List<String> getCompletionsForMember(Player player, String input) {
        Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
        Member member = clan.getMember(player.getUniqueId());
        if (member == null) return List.of();

        Set<RankPerm> perms = member.getRank().perms();

        List<String> completions = Arrays.stream(ClanSubcommand.values())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList());

        completions.removeIf(cmd -> switch (cmd) {
            case "setbase"          -> !perms.contains(RankPerm.SETBASE);
            case "base"             -> !perms.contains(RankPerm.BASE);
            case "invite"           -> !perms.contains(RankPerm.INVITE);
            case "withdraw"         -> !perms.contains(RankPerm.WITHDRAW);
            case "deposit", "invest"-> !perms.contains(RankPerm.DEPOSIT);
            case "kick"             -> !perms.contains(RankPerm.KICK);
            case "pvp"              -> !perms.contains(RankPerm.PVP);
            case "setslogan"        -> !plugin.getModules().isSlogan() || !perms.contains(RankPerm.SETSLOGAN);
            case "setprefix"        -> !plugin.getModules().isSetprefix() || !perms.contains(RankPerm.SETPREFIX);
            case "create", "accept" -> true;
            default                 -> false;
        });

        for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
            ExtendedGui gui = GuiLoader.getGuiConfiguration(entry.getKey());
            if (gui == null) continue;
            if (ActionUtil.evaluate(player, gui.getPreOpenExpressions(), ActionUtil.EvaluateMode.ALL)) {
                entry.getValue().stream()
                        .filter(str -> str.toLowerCase().startsWith(input.toLowerCase()))
                        .forEach(completions::add);
            }
        }

        return completions.stream()
                .filter(cmd -> cmd.startsWith(input.toLowerCase()))
                .toList();
    }
}