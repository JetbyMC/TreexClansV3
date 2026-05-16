package org.jetby.clans.api.service.clan.member.rank;

import java.util.Set;

public record Rank(
        String id,
        String name,
        Set<RankPerm> perms
) {
}