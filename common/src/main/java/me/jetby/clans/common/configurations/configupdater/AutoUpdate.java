package me.jetby.clans.common.configurations.configupdater;

import me.jetby.clans.common.configurations.configupdater.versions.V1;
import me.jetby.clans.common.configurations.configupdater.versions.V2;
import me.jetby.clans.common.configurations.configupdater.versions.V3;

import java.util.HashSet;
import java.util.Set;


public class AutoUpdate {

    private static final Set<Updater> versions = new HashSet<>();

    static {
        versions.add(new V1());
        versions.add(new V2());
        versions.add(new V3());
    }

    public AutoUpdate(int currentVersion) {

        for (Updater updater : versions) {
            if (currentVersion == updater.version()) {
                updater.load();
            }
        }
    }


}
