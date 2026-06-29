package org.jetby.clans.api.service.clan.member.rank;

public enum RankPerm implements Permission {
    INVITE,
    KICK,
    BASE,
    SETBASE,
    SETRANK,
    DEPOSIT,
    WITHDRAW,
    SETSLOGAN,
    SETPREFIX,
    RENAME,
    PVP;

    @Override
    public String getId() {
        return this.name();
    }
}
