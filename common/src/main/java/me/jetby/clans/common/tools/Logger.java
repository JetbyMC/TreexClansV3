package me.jetby.clans.common.tools;

import me.jetby.libb.color.Serializer;
import org.bukkit.plugin.Plugin;

public final class Logger {


    private final Plugin plugin;

    public Logger(Plugin plugin) {
        this.plugin = plugin;
    }

    public void warn(String message) {
        plugin.getComponentLogger().warn(Serializer.UNIFIED.deserialize(message));
    }

    public void info(String message) {
        plugin.getComponentLogger().info(Serializer.UNIFIED.deserialize(message));
    }

    public void success(String message) {
        plugin.getComponentLogger().info(Serializer.UNIFIED.deserialize("&a" + message));
    }

    public void error(String message) {
        plugin.getComponentLogger().error(Serializer.UNIFIED.deserialize(message));
    }
}
