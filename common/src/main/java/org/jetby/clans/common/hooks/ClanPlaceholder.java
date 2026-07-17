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
        this.papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
                && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
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
        boolean inClan = plugin.getClanManager().lookup().isInClan(player.getUniqueId());
        Clan clan = inClan ? plugin.getClanManager().lookup().getClanByMember(player.getUniqueId()) : null;

        return switch (args[0].toLowerCase()) {
            case "id" -> {
                if (!inClan || clan == null) yield "";
                yield clan.getId();
            }
            case "tag" -> {
                if (!inClan || clan == null)
                    yield plugin.getCfg().getTagPlaceholder_noClan();

                yield plugin.getCfg().getTagPlaceholder_hasClan()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix() == null ? "" : clan.getPrefix());
            }
            case "prefix" -> {
                if (!inClan || clan == null)
                    yield plugin.getCfg().getPrefixPlaceholder_noClan();
                if (clan.getPrefix() == null)
                    yield plugin.getCfg().getPrefixPlaceholder_noPrefix()
                            .replace("{tag}", clan.getId());

                yield plugin.getCfg().getPrefixPlaceholder_hasPrefix()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix());
            }
            case "coin" -> {
                if (!inClan || clan == null) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getCoin());
            }
            case "slogan" -> {
                if (!inClan || clan == null) yield "";
                yield clan.getSlogan();
            }
            case "balance" -> {
                if (!inClan || clan == null) yield "0";
                yield String.valueOf(clan.getBalance());
            }
            case "level" -> {
                if (!inClan || clan == null) yield "0";
                if (args.length == 1) {
                    yield clan.getLevel().id();
                } else if (args.length == 2 && args[1].equalsIgnoreCase("name")) {
                    yield clan.getLevel().name();
                } else yield null;
            }
            case "exp" -> {
                if (!inClan || clan == null) yield "0";
                if (args.length == 1) {
                    yield String.valueOf(clan.getExp());
                } else if (args.length == 2 && args[1].equalsIgnoreCase("max")) {
                    yield String.valueOf(clan.getLevel().minExp());
                } else yield null;
            }
            case "leader" -> {
                if (!inClan || clan == null || args.length < 2) yield "";
                if (args[1].equalsIgnoreCase("name")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
                    yield offlinePlayer.getName() != null ? offlinePlayer.getName() : "";
                }
                yield null;
            }
            case "isleader" -> {
                if (!inClan || clan == null) yield "";
                Member member = clan.getMember(player.getUniqueId());
                yield clan.getLeader().equals(member) ? "true" : "false";
            }
            case "rank" -> {
                if (!inClan || clan == null) yield "";
                Member member = clan.getMember(player.getUniqueId());
                if (member == null) yield "";
                yield member.getRank().name();
            }
            case "addon" -> {
                if (args.length == 3 && args[1].equalsIgnoreCase("status")) {
                    String addon = args[2];
                    yield String.valueOf(plugin.getAddonManager().isAddonEnabled(addon));
                } else yield null;
            }
            default -> null;
        };
    }
}