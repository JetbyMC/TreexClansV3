package org.jetby.clans.common.storage;


import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.tools.FileLoader;
import org.jetby.clans.common.tools.ItemSerializer;
import org.jetby.clans.common.tools.LocationHandler;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YAML implements Storage {

    private final TreexClans plugin;
    private final File file;
    private final FileConfiguration configuration;

    public YAML(TreexClans plugin) {
        this.plugin = plugin;
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
    }

    private boolean somethingWrong = true;

    @Override
    public void load() {

        for (String clanId : configuration.getKeys(false)) {
            if (clanId.equals("clan-id")) continue;
            ConfigurationSection clan = configuration.getConfigurationSection(clanId);
            if (clan == null) continue;

            String prefix = clan.getString("prefix");
            Set<Member> memberImplSet = new HashSet<>();
            double balance = clan.getDouble("balance", 0.0);
            String level = clan.getString("level", "1");
            int clanExp = clan.getInt("exp", 0);
            boolean pvp = clan.getBoolean("pvp", false);
            String slogan = clan.getString("slogan", "");


            String leaderUUID = clan.getString("leader.uuid");
            if (leaderUUID == null || leaderUUID.isEmpty()) {
                plugin.getLogger().warning("Clan " + clanId + " has no leader UUID in storage.yml!");
                continue;
            }

            Map<String, Rank> ranks = new HashMap<>();
            ConfigurationSection ranksSection = clan.getConfigurationSection("ranks");
            if (ranksSection != null) {

                for (String key : ranksSection.getKeys(false)) {
                    String displayName = ranksSection.getString(key + ".display-name");
                    ConfigurationSection permission = ranksSection.getConfigurationSection(key + ".permissions");
                    if (permission == null) continue;
                    Set<RankPerm> perms = new HashSet<>();
                    for (String perm : permission.getKeys(false)) {
                        switch (perm.toLowerCase()) {
                            case "invite" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.INVITE);
                            }
                            case "kick" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.KICK);
                            }
                            case "base" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.BASE);
                            }
                            case "setbase" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETBASE);
                            }
                            case "setrank" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETRANK);
                            }
                            case "deposit" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.DEPOSIT);
                            }
                            case "withdraw" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.WITHDRAW);
                            }
                            case "pvp" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.PVP);
                            }
                            case "setslogan" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETSLOGAN);
                            }
                            case "setprefix" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETPREFIX);
                            }
                        }


                    }
                    ranks.put(key.toLowerCase(), new Rank(key.toLowerCase(), displayName, perms));
                }
            } else {
                ranks.putAll(plugin.getCfg().getDefaultRanks());
            }
            ConfigurationSection leaderSection = clan.getConfigurationSection("leader");
            if (leaderSection == null) continue;
            MemberImpl leader = getMember(leaderUUID, leaderSection, ranks);

            ConfigurationSection members = clan.getConfigurationSection("members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(key);
                    if (member == null) continue;
                    memberImplSet.add(getMember(key, member, ranks));
                }
            }

            Location base = LocationHandler.deserialize(clan.getString("base-location"));

            Map<Integer, ItemStack> chestItems = new HashMap<>();
            ConfigurationSection chest = clan.getConfigurationSection("chest");
            if (chest!=null) {
                for (String key : chest.getKeys(false)) {
                    int slot = Integer.parseInt(key);
                    ItemStack item = chest.getItemStack(key);
                    if (item==null) continue;
                    chestItems.put(slot, item);
                }

            }
            Storage.CLANS.put(clanId, new ClanImpl(clanId, prefix, leader, memberImplSet, ranks,
                    plugin.getCfg().getLevels().get(Integer.parseInt(level)),
                    balance, base, clanExp, pvp,chestItems, slogan, plugin));
        }

        somethingWrong = false;
    }

    @Override
    public void save() {

        try {
            if (!somethingWrong) {
                for (String key : configuration.getKeys(false)) {
                    configuration.set(key, null);
                }
            }

            for (String clanId : Storage.CLANS.keySet()) {
                Clan clan = Storage.CLANS.get(clanId);

                for (String key : clan.getRanks().keySet()) {
                    Rank rank = clan.getRanks().get(key);
                    configuration.set(clanId + ".ranks." + rank.id() + ".display-name", rank.name());
                    Set<RankPerm> perms = rank.perms();
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.ALWAYS", true);
                    for (RankPerm perm : perms) {
                        configuration.set(clanId + ".ranks." + rank.id() + ".permissions." + perm.name(), true);
                    }
                }

                configuration.set(clanId + ".slogan", clan.getSlogan());
                configuration.set(clanId + ".balance", clan.getBalance());
                configuration.set(clanId + ".level", clan.getLevel().id());
                configuration.set(clanId + ".exp", clan.getExp());
                configuration.set(clanId + ".pvp", clan.isPvp());


                var leader = clan.getLeader();
                configuration.set(clan.getId() + ".leader.uuid", leader.getUuid().toString());
                setMember(leader, clan, "leader");

                for (var memberImpl : clan.getMembers()) {
                    setMember(memberImpl, clan, "members." + memberImpl.getUuid());
                }

                configuration.set(clanId + ".chest",
                        clan.getChest().values().stream()
                                .map(ItemSerializer::itemToBase64)
                                .toList());

                for (Map.Entry<Integer, ItemStack> entry : clan.getChest().entrySet()) {
                    if (entry.getValue()==null || entry.getValue().getType()== Material.AIR) continue;
                    configuration.set(clanId + ".chest."+entry.getKey(), entry.getValue());
                }


                Location location = clan.getBase();
                if (location != null) {
                    configuration.set(clanId + ".base-location", LocationHandler.serialize(clan.getBase()));
                } else {
                    configuration.set(clanId + ".base-location", null);
                }
            }
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MemberImpl getMember(String key, ConfigurationSection member, Map<String, Rank> ranks) {
        if (member == null) return null;
        UUID uuid = UUID.fromString(key);
        Rank rank = ranks.get(member.getString("rank"));
        long joinedAt = member.getLong("joined-at");
        long lastOnline = member.getLong("last-online");
        boolean glow = member.getBoolean("clan-glow", false);
        int coin = member.getInt("coin", 0);
        int exp = member.getInt("exp", 0);
        Map<UUID, Color> colors = new HashMap<>();
        for (String str : member.getStringList("glow-colors")) {
            String[] args = str.split(";");
            if (args.length < 4) continue;
            UUID id = UUID.fromString(args[0]);
            int r = Integer.parseInt(args[1]);
            int g = Integer.parseInt(args[2]);
            int b = Integer.parseInt(args[3]);
            Color color = Color.fromRGB(r, g, b);
            colors.put(id, color);
        }

        return new MemberImpl(uuid, rank, joinedAt, lastOnline, glow, false, coin, exp, colors,
                member.getInt("kills", 0),
                member.getInt("deaths", 0)
        );
    }

    private void setMember(Member member, Clan clanImpl, String path) {
        configuration.set(clanImpl.getId() + "." + path + ".rank", member.getRank().id());
        configuration.set(clanImpl.getId() + "." + path + ".joined-at", member.getJoinedAt());
        configuration.set(clanImpl.getId() + "." + path + ".last-online", member.getLastOnline());

        configuration.set(clanImpl.getId() + "." + path + ".coin", member.getCoin());
        configuration.set(clanImpl.getId() + "." + path + ".exp", member.getExp());
        configuration.set(clanImpl.getId() + "." + path + ".kills", member.getKills());
        configuration.set(clanImpl.getId() + "." + path + ".deaths", member.getDeaths());
    }
}
