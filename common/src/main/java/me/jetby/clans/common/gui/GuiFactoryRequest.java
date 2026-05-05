package me.jetby.clans.common.gui;

import lombok.Builder;
import lombok.NonNull;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.common.TreexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@Builder
public record GuiFactoryRequest(@NonNull TreexClans plugin, @NonNull Player player,
                                @NonNull FileConfiguration configuration, @Nullable Clan clan,
                                @Nullable Member target,
                                @Nullable Rank rank,
                                @Nullable FileConfiguration permissionConfig) {
}
