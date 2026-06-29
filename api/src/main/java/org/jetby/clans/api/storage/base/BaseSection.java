package org.jetby.clans.api.storage.base;

import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BaseSection {

    CompletableFuture<Void> set(String key, Object value);
    CompletableFuture<Void> set(Clan clan, String key, Object value);
    CompletableFuture<Void> set(Clan clan, Member member, String key, Object value);


    CompletableFuture<Object> get(String key);
    CompletableFuture<Object> get(Clan clan, String key);
    CompletableFuture<Object> get(Clan clan, Member member, String key);

    CompletableFuture<String> getString(String key);
    CompletableFuture<String> getString(Clan clan, String key);
    CompletableFuture<String> getString(Clan clan, Member member, String key);

    CompletableFuture<List<?>> getList(String key);
    CompletableFuture<List<?>> getList(Clan clan, String key);
    CompletableFuture<List<?>> getList(Clan clan, Member member, String key);

    CompletableFuture<List<String>> getStringList(String key);
    CompletableFuture<List<String>> getStringList(Clan clan, String key);
    CompletableFuture<List<String>> getStringList(Clan clan, Member member, String key);

    CompletableFuture<Integer> getInt(String key);
    CompletableFuture<Integer> getInt(Clan clan, String key);
    CompletableFuture<Integer> getInt(Clan clan, Member member, String key);

    CompletableFuture<Double> getDouble(String key);
    CompletableFuture<Double> getDouble(Clan clan, String key);
    CompletableFuture<Double> getDouble(Clan clan, Member member, String key);

    CompletableFuture<Long> getLong(String key);
    CompletableFuture<Long> getLong(Clan clan, String key);
    CompletableFuture<Long> getLong(Clan clan, Member member, String key);

    CompletableFuture<Boolean> getBoolean(String key);
    CompletableFuture<Boolean> getBoolean(Clan clan, String key);
    CompletableFuture<Boolean> getBoolean(Clan clan, Member member, String key);



}
