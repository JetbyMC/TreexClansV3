package me.jetby.clans.common.clan.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import org.bukkit.Color;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class MemberImpl implements Member {
    private UUID uuid;
    private Rank rank;
    private long joinedAt;
    private long lastOnline;
    private boolean clanGlow;
    private boolean chat;
    private int coin;
    private int exp;
    private Map<UUID, Color> glowColors;
    private int kills;
    private int deaths;

    public synchronized void addCoin(int a) {
        coin = coin + a;
    }

    public synchronized void takeCoin(int a) {
        coin = coin - a;
    }
}
