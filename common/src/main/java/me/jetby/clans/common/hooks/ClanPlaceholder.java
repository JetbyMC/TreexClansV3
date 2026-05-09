package me.jetby.clans.common.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.TreexClans;
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

            case "top" -> {
                if (args[1].equalsIgnoreCase("kills")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.KILLS, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.KILLS));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("deaths")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.DEATHS, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.DEATHS));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("kd")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.KD, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((double) plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.KD));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("balance")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.BALANCE, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((double) plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.BALANCE));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("level")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.LEVEL, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf(plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.LEVEL));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("members")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        Clan targetClan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.MEMBERS, num);
                        if (targetClan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield targetClan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(targetClan, LeaderboardService.TopType.MEMBERS));
                        }
                    }
                }
                yield null;
            }

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
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield clan.getLevel().id();
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
            case "rank" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "";
                yield member.getRank().name();
            }
            default -> null;
        };
    }
}
