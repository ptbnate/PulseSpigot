package xyz.krypton.spigot.util;

import static java.lang.Boolean.parseBoolean;

public final class PropertyHelper {

    private PropertyHelper() {
    }

    public static boolean getBoolean(String name, boolean def) {
        boolean result = def;
        try {
            result = parseBoolean(System.getProperty(name));
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        return result;
    }

}
