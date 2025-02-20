package xyz.krypton.spigot.util.yaml;

import com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class YamlConfig {

    private final File file;
    private final YamlConfiguration config = new YamlConfiguration();
    private final String fileName;

    public YamlConfig(String fileName) {
        this.fileName = fileName;

        file = new File(fileName);
        reload();
    }

    public void reload() {
        try {
            boolean createdFile = false;

            if (!file.exists())
                createdFile = file.createNewFile();

            config.load(file);

            if (createdFile) {
                config.save(file);
            }
        } catch (IOException | InvalidConfigurationException exception) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Could not load " + (fileName.contains(".yml") ? fileName : (fileName + ".yml")),
                    exception);
            Throwables.propagateIfPossible(exception);
            throw new RuntimeException(exception);
        }

        config.options().copyDefaults(true);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Could not save " + (fileName.contains(".yml") ? fileName : (fileName + ".yml")),
                    e);
        }
    }

    public void saveAsync() {
        CompletableFuture.runAsync(this::save);
    }
}
