package org.jetby.clans.common.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.storage.base.BaseSection;
import org.jetby.clans.common.tools.FileLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class YamlStorageCore extends StorageCore {

    private final File file;
    private final FileConfiguration configuration;

    public YamlStorageCore() {
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
    }


    @Override
    public void initialize() {
        initBaseSection();
        for (String key : configuration.getKeys(false)) {
            getClan(key);
        }
    }

    @Override
    public void shutdown() {
        for (Clan clan : cache.values()) {
            saveClan(clan);
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean clanExists(@NotNull String name) {
        return cache.containsKey(name) || configuration.contains(name);
    }

    @Override
    public boolean deleteClan(@NotNull String name) {
        cache.remove(name);
        configuration.set(name, null);
        return true;
    }

    @Override
    public @Nullable Clan getClan(@NotNull String name) {
        if (cache.containsKey(name)) return cache.get(name);
        if (!configuration.contains(name)) return null;
        return loadClan(name);
    }


    private void initBaseSection() {
        this.section = new YamlSection("");
    }

    private final class YamlSection implements BaseSection {

        private final String path;

        private YamlSection(String path) {
            this.path = path == null ? "" : path;
        }

        private String full(String key) {
            if (path.isEmpty()) {
                return key;
            }
            if (key == null || key.isEmpty()) {
                return path;
            }
            return path + "." + key;
        }

        @Override
        public BaseSection of(Clan clan) {
            return new YamlSection(clan.getId());
        }

        @Override
        public BaseSection of(Clan clan, Member member) {

            String memberPath = clan.getId() + ".members." + member.getUuid();

            return new YamlSection(memberPath);
        }

        @Override
        public BaseSection section(String name) {
            return new YamlSection(full(name));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return CompletableFuture.supplyAsync(() -> {
                if (path.isEmpty()) {
                    return configuration.getKeys(false);
                }

                var section = configuration.getConfigurationSection(path);
                if (section == null) {
                    return Collections.emptySet();
                }

                return section.getKeys(false);
            });
        }


        @Override
        public CompletableFuture<Void> set(String key, Object value) {
            return CompletableFuture.runAsync(() -> configuration.set(full(key), value));
        }

        @Override
        public CompletableFuture<Object> get(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.get(full(key)));
        }

        @Override
        public CompletableFuture<String> getString(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getString(full(key)));
        }

        @Override
        public CompletableFuture<Integer> getInt(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getInt(full(key)));
        }

        @Override
        public CompletableFuture<Double> getDouble(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getDouble(full(key)));
        }

        @Override
        public CompletableFuture<Long> getLong(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getLong(full(key)));
        }

        @Override
        public CompletableFuture<Boolean> getBoolean(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getBoolean(full(key)));
        }

        @Override
        public CompletableFuture<List<?>> getList(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getList(full(key)));
        }

        @Override
        public CompletableFuture<List<String>> getStringList(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getStringList(full(key)));
        }
    }
}