package org.jetby.clans.common.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetby.clans.common.tools.FileLoader;

@Getter
public class ModulesConfiguration {

    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration = FileLoader.getFileConfiguration("modules.yml");

    private boolean slogan;
    private boolean setprefix;

    public void load() {
        slogan = configuration.getBoolean("setslogan", true);
        setprefix = configuration.getBoolean("setprefix", true);
    }
}
