package org.bukkit.craftbukkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public final class Versioning {

    private static String bukkitVersion;

    public static String getBukkitVersion() {
        if (bukkitVersion != null) {
            return bukkitVersion;
        }

        InputStream stream = Bukkit.class.getClassLoader().getResourceAsStream("META-INF/maven/xyz.krypton.spigot/pulsespigot-api/pom.properties"); // PulseSpigot
        Properties properties = new Properties();

        if (stream != null) {
            try {
                properties.load(stream);
                bukkitVersion = properties.getProperty("version");
                return bukkitVersion;
            } catch (IOException ex) {
                Logger.getLogger(Versioning.class.getName()).log(Level.SEVERE, "Could not get Bukkit version!", ex);
            }
        }

        return "Unknown-Version";
    }
}
