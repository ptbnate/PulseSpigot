package xyz.krypton.spigot.config.legacy;

import eu.okaeri.configs.annotation.Exclude;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.krypton.spigot.config.PulseConfigSection;
import xyz.krypton.spigot.util.PropertyHelper;
import xyz.krypton.spigot.util.ReflectionHelper;

public final class LegacyRemapper {

    private static final boolean DISABLE_CONFIG_REMAPPING = PropertyHelper.getBoolean("PulseSpigot.disableLegacyConfigRemapping", false);

    private LegacyRemapper() {
    }

    public static void map(@NotNull Object toMap, @Nullable Object target) {
        if (DISABLE_CONFIG_REMAPPING) {
            return;
        }

        Class<?> clazz = toMap.getClass();
        RemapTarget staticTarget = clazz.getAnnotation(RemapTarget.class);
        if (staticTarget == null) {
            return;
        }
        Class<?> targetClazz = staticTarget.target();
        map(toMap, target, targetClazz);
    }

    private static void map(@NotNull Object toMap, @Nullable Object target, @NotNull Class<?> targetClazz) {
        for (Field field : ReflectionHelper.getFields(toMap.getClass())) {
            String sourceName = field.getName();
            String targetName = "UNKNOWN";
            try {
                field.setAccessible(true);
                Object value = field.get(Modifier.isStatic(field.getModifiers()) ? null : toMap);
                if (value == null) {
                    continue;
                }

                Remap mapping = field.getAnnotation(Remap.class);
                if (mapping == null) {
                    if (toMap.equals(value)
                            || !PulseConfigSection.class.isAssignableFrom(value.getClass())
                            || field.getAnnotation(Exclude.class) != null) {
                        continue;
                    }
                    map(value, target, targetClazz);
                    continue;
                }
                String legacyKey = mapping.value();
                targetName = legacyKey;

                try {
                    Field targetField = targetClazz.getDeclaredField(legacyKey);
                    targetField.setAccessible(true);
                    if (target == null && !Modifier.isStatic(targetField.getModifiers())) {
                        Bukkit.getLogger().log(Level.SEVERE, String.format("%s's field '%s' couldn't been mapped to %s's field '%s', because it's not static and target is null", toMap.getClass().getName(), sourceName, targetClazz.getName(), targetName));
                        continue;
                    }
                    targetField.set(target, value);
                } catch (NoSuchFieldException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, String.format("%s's field '%s' couldn't been mapped to %s's field '%s', because it doesn't exist", toMap.getClass().getName(), sourceName, targetClazz.getName(), targetName), ex);

                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.SEVERE, String.format("%s's field '%s' couldn't been mapped to %s's field '%s'", toMap.getClass().getName(), sourceName, targetClazz.getName(), targetName), ex);
            }
        }
    }

}
