package org.jetby.clans.api.service.clan.member.rank;

public enum RankPerm implements Permission {
    // ALWAYS permission is required because if some clan has 0 enabled perms, then the storage can be broke on loading
    ALWAYS,
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
        return this.name().toUpperCase();
    }
}
