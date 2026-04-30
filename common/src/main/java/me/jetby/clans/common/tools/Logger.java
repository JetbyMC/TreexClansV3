package me.jetby.clans.common.tools;

import org.bukkit.Bukkit;

public final class Logger {



    public  void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§e[TreexBuyer] §e" + message);
    }

    public  void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[TreexBuyer] §f" + message);
    }

    public  void success(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[TreexBuyer] §a" + message);
    }

    public  void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§c[TreexBuyer] §c" + message);
    }
}
