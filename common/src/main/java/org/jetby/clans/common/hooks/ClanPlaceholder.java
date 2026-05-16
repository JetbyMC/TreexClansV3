package org.jetby.clans.common.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.common.TreexClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanPlaceholder extends PlaceholderExpansion {
    private final TreexClans plugin;
    @Getter
    private final boolean papi;

    public ClanPlaceholder(TreexClans plugin) {
        this.plugin = plugin;
        this.papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.valueOf(plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String[] args = identifier.split("_");
        Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
        Member member = clan.getMember(player.getUniqueId());

        return switch (args[0].toLowerCase()) {
            case "id" -> clan.getId();
            case "tag" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getTagPlaceholder_noClan();

                yield plugin.getCfg().getTagPlaceholder_hasClan()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix() == null ? "" : clan.getPrefix());
            }
            case "prefix" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getPrefixPlaceholder_noClan();
                if (clan.getPrefix() == null) yield plugin.getCfg().getPrefixPlaceholder_noPrefix()
                        .replace("{tag}", clan.getId());

                yield plugin.getCfg().getPrefixPlaceholder_hasPrefix()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix());
            }
            case "coin" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getCoin());
            }
            case "slogan" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "";
                yield clan.getSlogan();
            }
            case "balance" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getBalance());
            }
            case "level" -> {
                if (args.length==1) {
                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                    yield clan.getLevel().id();
                } else if (args.length==2 && args[1].equalsIgnoreCase("name")) {
                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                    yield clan.getLevel().name();
                } else yield null;
            }
            case "exp" -> {
                if (args.length==1) {
                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                    yield String.valueOf(clan.getExp());
                } else if (args.length==2 && args[1].equalsIgnoreCase("max")) {
                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                    yield String.valueOf(clan.getLevel().minExp());
                } else yield null;
            }
            case "leader" -> {
                if (args[1].equalsIgnoreCase("name")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
                    yield offlinePlayer.getName();
                }
                yield null;
            }
            case "isleader" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "";
                yield member==clan.getLeader() ? "true" : "false";
            }
            case "rank" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "";
                yield member.getRank().name();
            }
            default -> null;
        };
    }
}
