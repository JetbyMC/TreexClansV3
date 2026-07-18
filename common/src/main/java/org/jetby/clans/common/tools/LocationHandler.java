package org.jetby.clans.common.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class LocationHandler {

    public String serialize(Location loc) {
        return loc.getX() + ";" +
                loc.getY() + ";" +
                loc.getZ() + ";" +
                loc.getWorld().getName();
    }

    public String serialize(Location loc, boolean yawPitch) {
        return loc.getX() + ";" +
                loc.getY() + ";" +
                loc.getZ() + ";" +
                loc.getYaw() + ";" +
                loc.getPitch() + ";" +
                loc.getWorld().getName();

    }

    @Nullable
    public Location deserialize(String str) {
        if (str == null || str.isEmpty() || str.equals("0;0;0;world")) {
            return null;
        }

        try {
            String[] parts = str.split(";");
            if (parts.length == 4) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                World world = Bukkit.getWorld(parts[3]);
                return world != null ? new Location(world, x, y, z) : null;
            }
            if (parts.length == 6) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                float yaw = Float.parseFloat(parts[3]);
                float pitch = Float.parseFloat(parts[4]);
                World world = Bukkit.getWorld(parts[5]);
                return world != null ? new Location(world, x, y, z, yaw, pitch) : null;
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }
}