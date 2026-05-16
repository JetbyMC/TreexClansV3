package org.jetby.clans.common.storage;

import org.jetby.clans.api.service.clan.Clan;

import java.util.HashMap;
import java.util.Map;

public interface Storage {
    Map<String, Clan> CLANS = new HashMap<>();

    void load();
    void save();
}
