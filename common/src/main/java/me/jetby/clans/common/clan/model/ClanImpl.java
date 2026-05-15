package me.jetby.clans.common.clan.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.level.Level;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.common.TreexClans;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@Getter
@Setter
public class ClanImpl implements Clan {
    @Setter(AccessLevel.NONE)
    private final String id;
    private String prefix;
    @Setter(AccessLevel.NONE)
    private final Member leader;
    private final Set<Member> members;
    private final Map<String, Rank> ranks;
    private Level level;
    private double balance;
    private Location base;
    private int exp;
    private boolean pvp;
    private Map<Integer, ItemStack> chest;
    private String slogan;

    private final TreexClans plugin;

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

    public @NotNull Set<Member> getMembersWithLeader() {
        Set<Member> list = new HashSet<>(members);
        list.add(leader);
        return list;
    }

    public void removeMember(@NotNull Member memberImpl) {
        this.members.remove(memberImpl);
    }


    public synchronized void addExp(int amount, @NotNull Member memberImpl, @NotNull Map<Integer, Level> levels) {
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

        memberImpl.setExp(memberImpl.getExp() + amount);
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

    public synchronized void takeExp(int a, @NotNull Member memberImpl) {
        setExp(getExp() - a);
        memberImpl.setExp(memberImpl.getExp() - a);
    }

    public synchronized void takeExp(int a) {
        setExp(getExp() - a);
    }

}
