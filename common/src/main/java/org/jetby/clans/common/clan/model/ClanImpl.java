package org.jetby.clans.common.clan.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.common.TreexClans;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionExecute;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@Getter
@Setter
public class ClanImpl implements Clan {
    private String id;
    private String prefix;
    @Setter(AccessLevel.NONE)
    private Member leader;
    private final Set<Member> members;
    private final Map<String, Rank> ranks;
    private Level level;
    @Setter(AccessLevel.NONE)
    private double balance;
    private Location base;
    private int exp;
    private boolean pvp;
    private Map<Integer, ItemStack> chest;
    private String slogan;

    private final TreexClans plugin = TreexClans.getInstance();

    public void addMember(@NotNull Member member) {
        this.members.add(member);
    }

    public Member getMember(@NotNull UUID uuid) {
        if (leader.getUuid().equals(uuid)) {
            return leader;
        }
        return members.stream()
                .filter(member -> member.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public synchronized void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public synchronized void transfer(Member target) {
        if (!members.contains(target)) return;

        Member leader = this.leader;
        leader.setRank(plugin.getCfg().getDefaultRank());
        members.add(leader);

        target.setRank(plugin.getCfg().getLeaderRank());
        this.leader = target;
    }

    public @NotNull Set<Member> getMembersWithLeader() {
        Set<Member> list = new HashSet<>(members);
        list.add(leader);
        return list;
    }

    public void removeMember(@NotNull Member member) {
        this.members.remove(member);
    }

    public synchronized void addExp(int amount, @NotNull Member member, @NotNull Map<Integer, Level> levels) {
        int remaining = amount;

        while (remaining > 0) {
            int toNext = level.minExp() - getExp();

            if (remaining >= toNext) {
                setExp(0);
                remaining -= toNext;

                Level nextLevel = levels.get(Integer.parseInt(level.id()) + 1);
                if (nextLevel == null) break;

                for (Member m : getMembersWithLeader()) {
                    ActionExecute.run(
                            ActionContext.of(Bukkit.getPlayer(m.getUuid()), plugin)
                                    .with(this)
                                    .with(m), nextLevel.levelUpActions());
                }

                setLevel(nextLevel);
            } else {
                setExp(getExp() + remaining);
                remaining = 0;
            }
        }

        member.setExp(member.getExp() + amount);
    }

    public synchronized void addExp(int amount, @NotNull Map<Integer, Level> levels) {
        int remaining = amount;

        while (remaining > 0) {
            int toNext = level.minExp() - getExp();

            if (remaining >= toNext) {
                setExp(0);
                remaining -= toNext;

                Level nextLevel = levels.get(Integer.parseInt(level.id()) + 1);
                if (nextLevel == null) break;

                for (Member m : getMembersWithLeader()) {
                    ActionExecute.run(
                            ActionContext.of(Bukkit.getPlayer(m.getUuid()), plugin)
                                    .with(this)
                                    .with(m), nextLevel.levelUpActions());
                }

                setLevel(nextLevel);
            } else {
                setExp(getExp() + remaining);
                remaining = 0;
            }
        }
    }


    public int getExpToNextLevel() {
        return level.minExp() - exp;
    }

    public synchronized void takeExp(int a, @NotNull Member member) {
        setExp(getExp() - a);
        member.setExp(member.getExp() - a);
    }

    public synchronized void takeExp(int a) {
        setExp(getExp() - a);
    }

}
