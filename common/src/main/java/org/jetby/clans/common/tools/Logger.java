package org.jetby.clans.common.tools;

import org.jetby.libb.color.Serializer;
import org.bukkit.plugin.Plugin;

public final class Logger {


    private final Plugin plugin;

    public Logger(Plugin plugin) {
        this.plugin = plugin;
    }

    public void warn(String message) {
        org.jetby.libb.util.Logger.warn(plugin, message);
    }

    public void info(String message) {
        org.jetby.libb.util.Logger.info(plugin, message);
    }

    public void success(String message) {
        org.jetby.libb.util.Logger.info(plugin, Serializer.UNIFIED.deserialize("&a" + message));
    }

    public void error(String message) {
        org.jetby.libb.util.Logger.error(plugin, Serializer.UNIFIED.deserialize(message));
    }
    public void error(String message, Object... objects) {
        org.jetby.libb.util.Logger.error(plugin, message, objects);
    }
}
