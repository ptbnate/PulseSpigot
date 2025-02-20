package xyz.krypton.spigot.util.range;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberRange {

    private static final Pattern RANGE_PATTERN = Pattern.compile("(-?[\\d.*]+)-(-?[\\d.*]+)");

    private final Number min;
    private final Number max;

    public NumberRange(Number min, Number max) {
        this.min = min;
        this.max = max;
    }

    public NumberRange(String string) {
        Matcher matcher = RANGE_PATTERN.matcher(string);

        Number min = Integer.MIN_VALUE;
        Number max = Integer.MAX_VALUE;
        if (matcher.matches()) {
            min = parseNumber(matcher.group(1), Integer.MIN_VALUE);
            max = parseNumber(matcher.group(2), Integer.MAX_VALUE);
        }

        this.min = min;
        this.max = max;
    }

    public Number getMax() {
        return max;
    }

    public Number getMin() {
        return min;
    }

    public static <V> V inRange(Number value, Map<NumberRange, V> rangeMap) {
        return rangeMap.entrySet().stream()
                .filter((entry) -> isNumberInRange(entry, value))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private static boolean isNumberInRange(Map.Entry<NumberRange, ?> range, Number value) {
        return isNumberInRange(range.getKey(), value);
    }

    private static boolean isNumberInRange(NumberRange range, Number value) {
        Number min = range.getMin();
        Number max = range.getMax();

        float floatValue = value.floatValue();
        // If we have integer we could determine upper range
        if (min instanceof Integer && max instanceof Integer) {
            return floatValue >= min.intValue() && floatValue <= max.intValue();
        }
        // If we have float we can't so as upper value we use probably start of other range
        return floatValue >= min.floatValue() && floatValue < max.floatValue();
    }

    public static <V> String inRangeToString(Number value, Map<NumberRange, V> rangeMap, boolean color, boolean useValueIfNull) {
        V rangeValue = inRange(value, rangeMap);
        if (rangeValue == null && useValueIfNull) {
            return Objects.toString(value);
        }

        String rangeValueString = Objects.toString(rangeValue);
        return color
                ? ChatColor.translateAlternateColorCodes('&', rangeValueString)
                : rangeValueString;
    }

    public static String inRangeToString(Number value, List<RangeFormatting> formattingList, boolean color, boolean useValueIfNull) {
        return inRangeToString(value, RangeFormatting.toRangeMap(formattingList), color, useValueIfNull);
    }

    public static <V> String inRangeToString(Number value, Map<NumberRange, V> rangeMap) {
        return inRangeToString(value, rangeMap, false, false);
    }

    public static String inRangeToString(Number value, List<RangeFormatting> formattingList) {
        return inRangeToString(value, RangeFormatting.toRangeMap(formattingList));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NumberRange)) {
            return false;
        }
        NumberRange range = (NumberRange) obj;

        return this.min.equals(range.min) && this.max.equals(range.max);
    }

    private static Number parseNumber(String numberString, Number borderValue) {
        try {
            if (numberString.contains("*")) {
                return borderValue;
            }
            else {
                if (numberString.contains(".")) {
                    return Double.parseDouble(numberString);
                }
                else {
                    return Integer.parseInt(numberString);
                }
            }
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return borderValue;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return simplifyAndString(min)+"-"+simplifyAndString(max);
    }

    private static String simplifyAndString(Number number) {
        int intNumber = number.intValue();
        if (intNumber <= Integer.MIN_VALUE) {
            return "-*";
        }

        if (intNumber >= Integer.MAX_VALUE) {
            return "*";
        }

        if (number instanceof Integer) {
            return Integer.toString(intNumber);
        }

        return Float.toString(number.floatValue());
    }

}
