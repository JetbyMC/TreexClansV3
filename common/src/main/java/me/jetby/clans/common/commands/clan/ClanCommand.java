package me.jetby.clans.common.commands.clan;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.gui.ClanGuiData;
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

        ClanSubcommand resolved = resolveSubcommand(sub);

        if (resolved == ClanSubcommand.SETSLOGAN && !plugin.getModules().isSlogan()) return true;
        if (resolved == ClanSubcommand.SETPREFIX && !plugin.getModules().isSetprefix()) return true;

        if (resolved != null) {
            resolved.getSubcommand().onCommand(sender, subArgs);
        } else {
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

        ClanSubcommand sub = resolveSubcommand(args[0]);
        if (sub != null) {
            return sub.getSubcommand().onTabCompleter(sender, command, s, args);
        }
        return List.of();
    }

    private ClanSubcommand resolveSubcommand(String input) {
        String lower = input.toLowerCase();
        for (Map.Entry<ClanSubcommand, List<String>> entry : CommandsConfiguration.SUBCOMMAND_ALIASES.entrySet()) {
            if (entry.getValue().contains(lower)) {
                return entry.getKey();
            }
        }
        return null;
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

            ClanGuiData gui = GuiLoader.getGuiConfiguration(entry.getKey());
            if (gui == null) return false;

            boolean inClan = plugin.getClanManager().lookup().isInClan(player.getUniqueId());

            if (gui.getListenType()==ListenType.CLAN_ONLY && !inClan) return false;

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
        return inClan ? getCompletionsForMember(player, input) : getCompletionsForNonMember(player, input);
    }

    private List<String> getCompletionsForNonMember(Player player, String input) {
        List<String> completions = new ArrayList<>();

        for (ClanSubcommand sub : List.of(ClanSubcommand.CREATE, ClanSubcommand.ACCEPT)) {
            List<String> subAliases = CommandsConfiguration.getAliases(sub);
            if (subAliases != null) completions.addAll(subAliases);
        }

        for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
            ClanGuiData gui = GuiLoader.getGuiConfiguration(entry.getKey());
            if (gui == null) continue;

            boolean inClan = plugin.getClanManager().lookup().isInClan(player.getUniqueId());
            if (gui.getListenType()==ListenType.CLAN_ONLY && !inClan) {
                continue;
            }

            if (gui.getListenType() == ListenType.DEFAULT || gui.getListenType() == ListenType.TOP_CLANS) {
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

        List<String> completions = new ArrayList<>();

        for (Map.Entry<ClanSubcommand, List<String>> entry : CommandsConfiguration.SUBCOMMAND_ALIASES.entrySet()) {
            boolean excluded = isExcluded(entry, perms);

            if (!excluded) {
                completions.addAll(entry.getValue());
            }
        }

        for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
            ClanGuiData gui = GuiLoader.getGuiConfiguration(entry.getKey());
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

    private boolean isExcluded(Map.Entry<ClanSubcommand, List<String>> entry, Set<RankPerm> perms) {
        ClanSubcommand sub = entry.getKey();

        return switch (sub) {
            case SETBASE   -> !perms.contains(RankPerm.SETBASE);
            case BASE      -> !perms.contains(RankPerm.BASE);
            case INVITE    -> !perms.contains(RankPerm.INVITE);
            case WITHDRAW  -> !perms.contains(RankPerm.WITHDRAW);
            case DEPOSIT   -> !perms.contains(RankPerm.DEPOSIT);
            case KICK      -> !perms.contains(RankPerm.KICK);
            case PVP       -> !perms.contains(RankPerm.PVP);
            case SETSLOGAN -> !plugin.getModules().isSlogan() || !perms.contains(RankPerm.SETSLOGAN);
            case SETPREFIX -> !plugin.getModules().isSetprefix() || !perms.contains(RankPerm.SETPREFIX);
            case CREATE, ACCEPT -> true;
            default        -> false;
        };
    }
}