package org.jetby.clans.api.storage;

import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.Lifecycle;
import org.jetby.clans.api.service.clan.Lookup;
import org.jetby.clans.api.storage.base.BaseSection;

import java.util.List;

public interface Storage extends Lifecycle, Lookup {


    /* Get the plugin section */
    BaseSection getSection();


    List<Clan> getClanList(int limit);

    void initialize();

    void shutdown();
}
