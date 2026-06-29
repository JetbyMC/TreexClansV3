package org.jetby.clans.api.service.clan.member.rank;

import java.util.Set;

public record Rank(String id,
                   String name,
                   Set<Permission> perms
) {
    public boolean hasPermission(Permission permission) {
        return perms.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(permission.getId()));
    }
}