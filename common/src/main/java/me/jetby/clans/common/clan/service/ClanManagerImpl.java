package me.jetby.clans.common.clan.service;

import me.jetby.clans.api.events.ClanCreateEvent;
import me.jetby.clans.api.events.ClanDeleteEvent;
import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.level.Level;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.clans.common.clan.model.MemberImpl;
import me.jetby.clans.common.configurations.Config;
import me.jetby.clans.common.configurations.MessagesConfiguration;
import me.jetby.clans.common.storage.Storage;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.ActionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @NotNull Map<String, Clan> getClanList() {
        return Storage.CLANS;
    }

    private boolean exists(@NotNull String name) {
        return getClanList().containsKey(name);
    }

    private final class LifecycleImpl implements ClanManager.Lifecycle {

        @Override
        public boolean createClan(@NotNull String name, @NotNull Clan clan) {
            if (exists(name)) return false;

            var event = new ClanCreateEvent(clan, null);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                plugin.getLogger().info("Clan creation process halted — event cancelled externally.");
                return false;
            }

            getClanList().put(name, clan);
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
                    false,
                    0,
                    0,
                    new HashMap<>(),
                    0,
                    0
            );

            Level baseLevel = plugin.getCfg().getLevels().getOrDefault(
                    1,
                    new Level("1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>())
            );

            Clan clan = new ClanImpl(
                    name,
                    null,
                    leader,
                    new HashSet<>(),
                    plugin.getCfg().getDefaultRanks(),
                    baseLevel,
                    0.0,
                    null,
                    0,
                    false,
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>(),
                    "",
                    plugin
            );

            var event = new ClanCreateEvent(clan, leaderPlayer);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                plugin.getLogger().info("Clan creation process halted — event cancelled externally.");
                return false;
            }

            getClanList().put(name, clan);
            return true;
        }

        @Override
        public boolean deleteClan(@NotNull Clan clan, @Nullable Player initiator) {
            var event = new ClanDeleteEvent(clan, initiator);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                plugin.getLogger().info("Clan delete process halted — event cancelled externally.");
                return false;
            }

            getClanList().remove(clan.getId());
            return true;
        }

        @Override
        public boolean deleteClan(@NotNull String name) {
            var clan = getClanList().get(name);
            if (clan == null) {
                return false;
            }

            var event = new ClanDeleteEvent(clan, null);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                plugin.getLogger().info("Clan delete process halted — event cancelled externally.");
                return false;
            }

            // notify members
            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage("Your clan was disbanded by clan leader");
                }
            }

            getClanList().remove(clan.getId());
            return true;
        }

        @Override
        public boolean clanExists(@NotNull String name) {
            return exists(name);
        }
    }

    private final class ValidationImpl implements ClanManager.Validation {

        @Override
        public boolean isAllowedName(@NotNull Player player, @NotNull String clanName) {
            int min = plugin.getCfg().getMinTagLength();
            int max = plugin.getCfg().getMaxTagLength();

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

            if (!isAllowedRegex(clanName, plugin.getCfg().getRegex())) {
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
            String cleaned = removeIgnoredSymbols(prefix, plugin.getCfg().getLengthIgnoredSymbols());
            int min = plugin.getCfg().getPrefixMinLength();
            int max = plugin.getCfg().getPrefixMaxLength();

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

            if (!isAllowedRegex(prefix, plugin.getCfg().getPrefixRegex())) {
                plugin.getMessages().of(player, "disallowed-prefix-regex")
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

            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(message);
                }
            }

            var leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(message);
            }
        }

        @Override
        public void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message) {
            String format = plugin.getCfg().getChatFormat()
                    .replace("{player}", sender.getName())
                    .replace("{message}", message);

            Component colored = Config.CONFIG_COLORIZER.deserialize(format);

            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(colored);
                }
            }

            var leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(colored);
            }
        }
    }

    private static final class EconomyImpl implements ClanManager.Economy {

        @Override
        public void addBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() + amount);
        }

        @Override
        public void takeBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() - amount);
        }

        @Override
        public double getBalance(@NotNull Clan clan) {
            return clan.getBalance();
        }
    }

    private final class LookupImpl implements ClanManager.Lookup {

        @Override
        public boolean isInClan(@NotNull UUID uuid) {
            return getClanList().values().stream()
                    .anyMatch(clan ->
                            (clan.getLeader().getUuid().equals(uuid)) ||
                                    clan.getMembers().stream().anyMatch(m -> m.getUuid().equals(uuid))
                    );
        }

        @Override
        @Deprecated(since = "Это так не работает если не ошибаюсь")
        public boolean isInClan(@NotNull String uuidString) {
            try {
                return isInClan(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public @Nullable Clan getClan(@NotNull String name) {
            return getClanList().get(name);
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull UUID uuid) {
            return getClanList().values().stream()
                    .filter(clan ->
                            (clan.getLeader().getUuid().equals(uuid)) ||
                                    clan.getMembers().stream().anyMatch(m -> m.getUuid().equals(uuid))
                    )
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull String uuidString) {
            try {
                return getClanByMember(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull Member member) {
            return getClanList().values().stream()
                    .filter(clan ->
                            (clan.getLeader().equals(member)) ||
                                    clan.getMembers().contains(member)
                    )
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public @NotNull String getLastOnlineFormatted(@NotNull UUID uuid) {
            if (!isInClan(uuid)) {
                return "-1";
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            ClanImpl clan = (ClanImpl) getClanByMember(uuid);
            if (clan == null) {
                return "-1";
            }

            var member = clan.getMember(uuid);
            if (member == null) {
                return "-1";
            }

            if (offline.isOnline()) {
                member.setLastOnline(System.currentTimeMillis());
                return "В сети";
            }

            long diff = System.currentTimeMillis() - member.getLastOnline();
            return plugin.getFormatTime().stringFormat(diff);
        }

        @Override
        public @NotNull String getLastOnlineFormatted(@NotNull Member member) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member.getUuid());

            if (offline.isOnline()) {
                member.setLastOnline(System.currentTimeMillis());
                return "В сети";
            }

            long diff = System.currentTimeMillis() - member.getLastOnline();
            return plugin.getFormatTime().stringFormat(diff);
        }
    }
}
