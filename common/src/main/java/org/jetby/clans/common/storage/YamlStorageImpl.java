package org.jetby.clans.common.storage;

import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YamlStorageImpl extends StorageCore {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "treexclans-storage-yaml")
    );

    private final File file;
    private final FileConfiguration configuration;

    public YamlStorageImpl() {
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
    }

    @Override
    public void initialize() {
        initBaseSection();
        for (String key : configuration.getKeys(false)) {
            loadClan(key);
        }
    }

    @Override
    public void shutdown() {
        for (Clan clan : cache.values()) {
            saveClan(clan);
        }
        CompletableFuture<Void> barrier = CompletableFuture.runAsync(() -> {}, executor);
        barrier.join();

        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
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
                return section == null ? Collections.emptySet() : section.getKeys(false);
            }, executor);
        }

        @Override
        public CompletableFuture<Void> remove(String key) {
            return CompletableFuture.runAsync(() -> configuration.set(full(key), null), executor);
        }

        @Override
        public CompletableFuture<Void> set(String key, Object value) {
            return CompletableFuture.runAsync(() -> configuration.set(full(key), value), executor);
        }

        @Override
        public CompletableFuture<Object> get(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.get(full(key)), executor);
        }

        @Override
        public CompletableFuture<String> getString(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getString(full(key)), executor);
        }

        @Override
        public CompletableFuture<Integer> getInt(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getInt(full(key)), executor);
        }

        @Override
        public CompletableFuture<Double> getDouble(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getDouble(full(key)), executor);
        }

        @Override
        public CompletableFuture<Long> getLong(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getLong(full(key)), executor);
        }

        @Override
        public CompletableFuture<Boolean> getBoolean(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getBoolean(full(key)), executor);
        }

        @Override
        public CompletableFuture<List<?>> getList(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getList(full(key)), executor);
        }

        @Override
        public CompletableFuture<List<String>> getStringList(String key) {
            return CompletableFuture.supplyAsync(() -> configuration.getStringList(full(key)), executor);
        }
    }
}