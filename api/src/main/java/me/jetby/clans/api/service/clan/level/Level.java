package me.jetby.clans.api.service.clan.level;

import java.util.List;

public record Level(
        String id,
        int minExp,
        int maxMembers,
        int maxBalance,
        int chest,
        List<String> quests,
        List<String> levelUpActions
) {
}
