package org.jetby.clans.api.service.clan.member.rank;

import org.jetby.clans.api.service.clan.member.Member;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionRegistry {
    private static final Map<String, Permission> REGISTRY = new ConcurrentHashMap<>();

    static {
        for (RankPerm perm : RankPerm.values()) {
            REGISTRY.put(perm.getId(), perm);
        }
    }

    public static Permission register(String id) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Permission already registered: " + id);
        }
        Permission perm = () -> id;
        REGISTRY.put(id, perm);
        return perm;
    }

    public static void unregister(String id) {
        REGISTRY.remove(id);
    }

    public static void unregister(Permission perm) {
        REGISTRY.remove(perm.getId());
    }

    public static Permission get(String id) {
        return REGISTRY.get(id);
    }
    public static Collection<Permission> getAll() {
        return REGISTRY.values();
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    public static boolean hasPermission(Member member, Permission permission) {
        return member.getRank().hasPermission(permission);
    }
    public static boolean hasPermission(Rank rank, Permission permission) {
        return rank.hasPermission(permission);
    }
}
