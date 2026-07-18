package org.jetby.clans.common.clan.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.events.ClanCreateEvent;
import org.jetby.clans.api.events.ClanDeleteEvent;
import org.jetby.clans.api.service.ClanManager;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.Lifecycle;
import org.jetby.clans.api.service.clan.Lookup;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.util.Papi;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.configurations.Config;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionExecute;
import org.jetby.libb.action.ActionUtil;

import java.util.*;

/**
 * Concrete implementation of {@link ClanManager} for TreexClans.
 * <p>
 * External plugins should use the {@link ClanManager} API interface,
 * this class is internal wiring between plugin config and API.
 */
public final class ClanManagerImpl implements Listener, ClanManager {

    private final TreexClans plugin;

    private final Lifecycle lifecycle = new LifecycleImpl();
    private final Validation validation = new ValidationImpl();
    private final Chat chat = new ChatImpl();
    private final Economy economy = new EconomyImpl();
    private final Lookup lookup = new LookupImpl();

    public ClanManagerImpl(@NotNull TreexClans plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    @Override
    public @NotNull Lifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public @NotNull ClanManager.Validation validation() {
        return validation;
    }

    @Override
    public @NotNull Chat chat() {
        return chat;
    }

    @Override
    public @NotNull Economy economy() {
        return economy;
    }

    @Override
    public @NotNull Lookup lookup() {
        return lookup;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Clan clan = lookup.getClanByMember(uuid);
            if (clan == null) return;
            Member member = clan.getMember(uuid);
            if (member instanceof MemberImpl impl) {
                impl.setLastOnline(System.currentTimeMillis());
            }
        });
    }

    @Override
    public @NotNull List<Clan> getClanList(int limit) {
        return plugin.getStorage().getClanList(limit);
    }

    private boolean exists(@NotNull String tag) {
        return plugin.getStorage().getCache().containsKey(tag);
    }

    private final class LifecycleImpl implements Lifecycle {

        @Override
        public boolean createClan(@NotNull String name, @NotNull Clan clan) {
            if (exists(name)) return false;


            var event = new ClanCreateEvent(clan, null);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }
            plugin.getStorage().saveClan(clan);
            return true;
        }

        @Override
        public boolean createClan(@NotNull String name, @NotNull Player leaderPlayer) {
            if (exists(name)) return false;

            MemberImpl leader = new MemberImpl(
                    leaderPlayer.getUniqueId(),
                    plugin.getCfg().getLeaderRank(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    false,
                    0,
                    0,
                    0,
                    0
            );

            Level baseLevel = plugin.getCfg().getLevels().getOrDefault(
                    1,
                    new Level("1", "1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>())
            );

            Clan clan = new ClanImpl(
                    name,
                    null,
                    leader,
                    new HashSet<>(),
                    plugin.getCfg().getRanks(),
                    baseLevel,
                    0.0,
                    null,
                    0,
                    false,
                    new HashMap<>(),
                    ""
            );

            var event = new ClanCreateEvent(clan, leaderPlayer);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            plugin.getStorage().saveClan(clan);

            return true;
        }

        @Override
        public boolean deleteClan(@NotNull Clan clan, @Nullable Player initiator) {

            var event = new ClanDeleteEvent(clan, initiator);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            plugin.getStorage().getCache().remove(clan.getId());
            plugin.getStorage().getSection().set(clan.getId(), null);
            return true;
        }


        @Override
        public boolean deleteClan(@NotNull String name) {

            var clan = plugin.getStorage().getCache().remove(name);
            if (clan == null) {
                return false;
            }

            var event = new ClanDeleteEvent(clan, null);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }
            plugin.getStorage().getCache().remove(clan.getId());
            plugin.getStorage().getSection().set(clan.getId(), null);
            return true;
        }

        @Override
        public boolean clanExists(@NotNull String name) {
            return exists(name);
        }

        @Override
        public boolean renameClan(@NotNull Clan clan, @NotNull String newId) {
            String oldId = clan.getId();
            if (exists(newId)) return false;

            Object data = plugin.getStorage().getSection().get(oldId);
            plugin.getStorage().getSection().set(oldId, null);
            plugin.getStorage().getSection().set(newId, data);
            plugin.getStorage().getCache().remove(oldId);
            ClanImpl impl = (ClanImpl) clan;
            impl.setId(newId);
            plugin.getStorage().getCache().put(newId, impl);

            return true;
        }
    }

    private final class ValidationImpl implements ClanManager.Validation {

        @Override
        public boolean isAllowedName(@NotNull Player player, @NotNull String clanName) {
            int min = plugin.getCfg().getCreateValidation().minLength();
            int max = plugin.getCfg().getCreateValidation().maxLength();

            if (clanName.length() < min) {
                plugin.getMessages().of(player, "clan-tag-too-short")
                        .replace("{min_length}", String.valueOf(min))
                        .run();
                return false;
            }

            if (clanName.length() > max) {
                plugin.getMessages().of(player, "clan-tag-too-long")
                        .replace("{max_length}", String.valueOf(max))
                        .run();
                return false;
            }

            if (plugin.getCfg().getBlockedTags().contains(clanName.toLowerCase())) {
                plugin.getMessages().of(player, "clan-tag-blocked")
                        .run();
                return false;
            }

            if (!isAllowedRegex(clanName, plugin.getCfg().getCreateValidation().regex())) {
                plugin.getMessages().of(player, "disallowed-tag-regex")
                        .run();
                return false;
            }

            // Requirements (money, perms, etc.)
            return ActionExecute.run(
                    ActionContext.of(player, plugin)
                            .replace("{name}", clanName), plugin.getCfg().getRequirements(), ActionUtil.EvaluateMode.ALL);

        }

        @Override
        public boolean isAllowedPrefix(@NotNull Player player, @NotNull String prefix) {
            String cleaned = removeIgnoredSymbols(prefix, plugin.getCfg().getPrefixValidation().lengthIgnoredSymbols());
            int min = plugin.getCfg().getPrefixValidation().minLength();
            int max = plugin.getCfg().getPrefixValidation().maxLength();

            if (cleaned.length() < min) {
                plugin.getMessages().of(player, "clan-prefix-too-short")
                        .replace("{min_length}", String.valueOf(min))
                        .run();
                return false;
            }

            if (cleaned.length() > max) {
                plugin.getMessages().of(player, "clan-prefix-too-long")
                        .replace("{max_length}", String.valueOf(max))
                        .run();
                return false;
            }

            if (plugin.getCfg().getBlockedTags().contains(prefix.toLowerCase())) {
                plugin.getMessages().of(player, "clan-tag-blocked")
                        .run();
                return false;
            }

            if (!isAllowedRegex(prefix, plugin.getCfg().getPrefixValidation().regex())) {
                plugin.getMessages().of(player, "disallowed-prefix-regex")
                        .run();
                return false;
            }

            return true;
        }

        @Override
        public boolean isAllowedSlogan(@NotNull Player player, @NotNull String slogan) {
            String cleaned = removeIgnoredSymbols(slogan, plugin.getCfg().getSloganValidation().lengthIgnoredSymbols());
            int min = plugin.getCfg().getPrefixValidation().minLength();
            int max = plugin.getCfg().getPrefixValidation().maxLength();

            if (cleaned.length() < min) {
                plugin.getMessages().of(player, "clan-slogan-too-short")
                        .replace("{min_length}", String.valueOf(min))
                        .run();
                return false;
            }

            if (cleaned.length() > max) {
                plugin.getMessages().of(player, "clan-slogan-too-long")
                        .replace("{max_length}", String.valueOf(max))
                        .run();
                return false;
            }

            if (!isAllowedRegex(slogan, plugin.getCfg().getPrefixValidation().regex())) {
                plugin.getMessages().of(player, "disallowed-slogan-regex")
                        .run();
                return false;
            }

            return true;
        }

        @Override
        public boolean isAllowedRegex(@NotNull String text, @NotNull String regex) {
            return text.matches(regex);
        }

        private String removeIgnoredSymbols(String input, String ignoredRegex) {
            if (ignoredRegex == null || ignoredRegex.isEmpty()) {
                return input;
            }
            return input.replaceAll(ignoredRegex, "");
        }
    }

    private final class ChatImpl implements ClanManager.Chat {

        @Override
        public void sendMessage(@NotNull Clan clan, @NotNull String message) {
            Component colored = Config.CONFIG_COLORIZER.deserialize(Papi.set(null, message));

            for (Member member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(colored);
                }
            }

            Member leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(colored);
            }
        }

        @Override
        public void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message) {
            String format = Papi.set(sender, plugin.getCfg().getChatFormat());

            format = format
                    .replace("{player}", sender.getName())
                    .replace("{message}", message);

            Component colored = Config.CONFIG_COLORIZER.deserialize(format);

            for (Member member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(colored);
                }
            }

            Member leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(colored);
            }
        }
    }

    private static final class EconomyImpl implements ClanManager.Economy {

        @Override
        public synchronized void addBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() + amount);
        }

        @Override
        public synchronized void takeBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() - amount);
        }

        @Override
        public double getBalance(@NotNull Clan clan) {
            return clan.getBalance();
        }
    }

    private final class LookupImpl implements Lookup {

        @Override
        public boolean isInClan(@NotNull UUID uuid) {
            return getClanByMember(uuid) != null;
        }

        @Override
        public @Nullable Clan getClan(@NotNull String name) {
            if (exists(name)) return plugin.getStorage().getCache().get(name);
            return plugin.getStorage().loadClan(name);
        }


        @Override
        public @Nullable Clan getClanByMember(@NotNull Member member) {
            return getClanByMember(member.getUuid());
        }


        @Override
        public @Nullable Clan getClanByMember(@NotNull UUID uuid) {
            for (Clan clan : plugin.getStorage().getCache().values()) {
                if (clan.getLeader().getUuid().equals(uuid)) return clan;
                if (clan.getMember(uuid) != null) return clan;
            }
            return null;
        }


    }

    @Override
    public @NotNull String getLastOnlineFormatted(@NotNull Member member) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(member.getUuid());

        if (offline.isOnline()) {
            member.setLastOnline(System.currentTimeMillis());
            return plugin.getMessages().getCleanMessage("status.online");
        }

        long diff = System.currentTimeMillis() - member.getLastOnline();
        return plugin.getFormatTime().stringFormat(diff);
    }
}
