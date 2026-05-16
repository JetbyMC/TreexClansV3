package org.jetby.clans.api.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.libb.color.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class GuiContext {
    @NotNull
    private final JavaPlugin plugin;
    @NotNull
    private final ClanGuiData gui;
    @NotNull
    private final Player player;
    @Nullable
    private final Clan clan;
    @Getter
    private final Serializer serializer;

    private final Map<Class<?>, Object> objects = new HashMap<>();

    /**
     * Add an object to the context. The key is the object's class.
     * If two objects of the same class are added, the second overwrites the first.
     */
    public <T> GuiContext with(@NotNull T object) {
        objects.put(object.getClass(), object);
        return this;
    }

    /**
     * Add an object under an explicit class key.
     * Useful when you want to retrieve it by interface rather than concrete class.
     *
     * <pre>{@code ctx.with(MyEntity.class, entity); }</pre>
     */
    public <T> GuiContext with(@NotNull Class<T> key, @NotNull T object) {
        objects.put(key, object);
        return this;
    }

    /**
     * Get an object by class. Returns {@code null} if it was not added.
     */
    @Nullable
    public <T> T get(@NotNull Class<T> type) {
        return type.cast(objects.get(type));
    }

    public static GuiContext of(@NotNull JavaPlugin plugin, @NotNull ClanGuiData gui, @NotNull Player player, @Nullable Clan clan) {
        return new GuiContext(plugin, gui, player, clan, Serializer.UNIFIED);
    }
    public static GuiContext of(@NotNull JavaPlugin plugin,
                                @NotNull ClanGuiData gui,
                                @NotNull Player player,
                                @Nullable Clan clan,
                                @NotNull Serializer serializer) {
        return new GuiContext(plugin, gui, player, clan, serializer);
    }
}
