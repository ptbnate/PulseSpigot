package me.nate.spigot.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class ReflectionHelper {

    private ReflectionHelper() {
    }

    public static @NotNull Set<Field> getFields(@NotNull Class<?> clazz) {
        Set<Field> fields = new LinkedHashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            fields.addAll(getFields(superClass));
        }
        return fields;
    }

}
