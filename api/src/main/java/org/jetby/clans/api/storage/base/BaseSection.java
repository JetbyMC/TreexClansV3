package org.jetby.clans.api.storage.base;

import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface BaseSection {

    BaseSection of(Clan clan);

    BaseSection of(Clan clan, Member member);

    BaseSection section(String name);

    CompletableFuture<Set<String>> keys();

    CompletableFuture<Void> remove(String key);

    CompletableFuture<Void> set(String key, Object value);

    CompletableFuture<Object> get(String key);

    CompletableFuture<String> getString(String key);

    CompletableFuture<Integer> getInt(String key);

    CompletableFuture<Double> getDouble(String key);

    CompletableFuture<Long> getLong(String key);

    CompletableFuture<Boolean> getBoolean(String key);

    CompletableFuture<List<?>> getList(String key);

    CompletableFuture<List<String>> getStringList(String key);

}
