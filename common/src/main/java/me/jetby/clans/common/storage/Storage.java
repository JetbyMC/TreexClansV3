package me.jetby.clans.common.storage;

import me.jetby.clans.api.service.clan.Clan;

import java.util.HashMap;
import java.util.Map;

public interface Storage {
    Map<String, Clan> CLANS = new HashMap<>();

    void load();
    void save();
}
