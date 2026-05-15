package me.jetby.clans.common.listeners;

import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class ClanListeners implements Listener {

    private final TreexClans plugin;
    private final ClanManager manager;

    public ClanListeners(TreexClans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
    }

    @Deprecated
    @EventHandler
    public void onClanChat(AsyncPlayerChatEvent e) {
        if (!plugin.getClanManager().lookup().isInClan(e.getPlayer().getUniqueId())) return;
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(e.getPlayer().getUniqueId());
        if (clanImpl == null) return;
        if (!clanImpl.getMember(e.getPlayer().getUniqueId()).isChat()) return;
        plugin.getClanManager().chat().sendChat(clanImpl, e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeamDamage(EntityDamageByEntityEvent e) {

        Player damager = null;

        if (e.getDamager() instanceof Player p) {
            damager = p;
        } else if (e.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player p) {
            damager = p;
        }

        if (damager==e.getEntity()) return;

        if (damager == null) return;
        if (!(e.getEntity() instanceof Player target)) return;

        var lookup = manager.lookup();

        Clan clan = lookup.getClanByMember(damager.getUniqueId());
        if (clan == null) return;
        if (clan.isPvp()) return;

        Clan targetClan = lookup.getClanByMember(target.getUniqueId());
        if (clan.equals(targetClan)) {
            e.setCancelled(true);
            plugin.getMessages().of(damager, "pvp-disabled")
                    .with(clan)
                    .run();
        }
    }

    @EventHandler
    public void onClanKillsOrDeaths(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();
        if (manager.lookup().isInClan(player.getUniqueId())) {
            Clan clan = manager.lookup().getClanByMember(player.getUniqueId());
            Member member = clan.getMember(player.getUniqueId());
            member.setDeaths(member.getDeaths() + 1);
        }
        if (killer != null) {
            if (manager.lookup().isInClan(killer.getUniqueId())) {
                Clan clan = manager.lookup().getClanByMember(killer.getUniqueId());
                Member member = clan.getMember(killer.getUniqueId());
                member.setKills(member.getKills() + 1);
            }
        }
    }
}
