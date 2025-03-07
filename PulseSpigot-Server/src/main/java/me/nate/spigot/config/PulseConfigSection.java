package me.nate.spigot.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.exception.OkaeriException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import me.nate.spigot.util.ReflectionHelper;

public abstract class PulseConfigSection extends OkaeriConfig {

    @Override
    public OkaeriConfig load() throws OkaeriException {
        super.load();
        this.loadPropertiesInternal();
        return this;
    }

    public void loadProperties() {
    }

    private void loadPropertiesInternal() {
        this.loadProperties();

        for (Field field : ReflectionHelper.getFields(this.getClass())) {
            if (field.getAnnotation(Exclude.class) != null) {
                continue;
            }

            try {
                field.setAccessible(true);

                Class<?> fieldType = field.getType();
                if (PulseConfigSection.class.isAssignableFrom(fieldType)) {
                    PulseConfigSection section = (PulseConfigSection) field.get(this);
                    if (section == null) {
                        continue;
                    }
                    section.loadPropertiesInternal();
                } else if (Collection.class.isAssignableFrom(fieldType)) {
                    Collection<?> collection = (Collection<?>) field.get(this);
                    if (collection == null) {
                        continue;
                    }
                    collection.forEach(value -> {
                        if (!(value instanceof PulseConfigSection)) {
                            return;
                        }
                        ((PulseConfigSection) value).loadPropertiesInternal();
                    });
                } else if (Map.class.isAssignableFrom(fieldType)) {
                    Map<?, ?> map = (Map<?, ?>) field.get(this);
                    if (map == null) {
                        continue;
                    }
                    map.forEach((key, value) -> {
                        if (!(value instanceof PulseConfigSection)) {
                            return;
                        }
                        ((PulseConfigSection) value).loadPropertiesInternal();
                    });
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to load properties for " + field.getName(), ex);
            }
            this.colorField(field);
        }
    }

    private void colorField(@NotNull Field field) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            if (String.class.isAssignableFrom(fieldType)) {
                field.set(this, color((String) field.get(this)));
                return;
            } else if (List.class.isAssignableFrom(fieldType)) {
                List<?> list = (List<?>) field.get(this);
                if (list == null || list.isEmpty()) {
                    return;
                }

                List<Object> newList = new ArrayList<>(list);
                newList.replaceAll(value -> {
                    if (!(value instanceof String)) {
                        return value;
                    }
                    return color((String) value);
                });

                field.set(this, newList);
                return;
            } else if (Map.class.isAssignableFrom(fieldType)) {
                Map<?, ?> map = (Map<?, ?>) field.get(this);
                if (map == null || map.isEmpty()) {
                    return;
                }

                Map<Object, Object> newMap = new LinkedHashMap<>();
                map.forEach((key, value) -> {
                    if (!(value instanceof String)) {
                        newMap.put(key, value);
                        return;
                    }
                    newMap.put(key, color((String) value));
                });

                field.set(this, newMap);
                return;
            }
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to color property " + field.getName(), ex);
        }
    }

    private static @NotNull String color(@NotNull String input) {
        return ChatColor.translateAlternateColorCodes(input);
    }

}
