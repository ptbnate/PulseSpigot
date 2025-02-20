package xyz.krypton.spigot.util.range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeFormatting {

    private final NumberRange range;
    private final String value;

    public RangeFormatting(NumberRange range, String value) {
        this.range = range;
        this.value = value;
    }

    public RangeFormatting(Number min, Number max, String value) {
        this(new NumberRange(min, max), value);
    }

    public RangeFormatting(String string) {
        String[] splitString = string.split(" ", 2);

        this.range = new NumberRange(splitString[0]);
        this.value = splitString[1];
    }

    public NumberRange getRange() {
        return range;
    }

    public String getValue() {
        return value;
    }

    public static Map<NumberRange, String> toRangeMap(List<RangeFormatting> rangeFormattingList) {
        Map<NumberRange, String> rangeMap = new HashMap<>();
        for (RangeFormatting rangeFormatting : rangeFormattingList) {
            rangeMap.put(rangeFormatting.getRange(), rangeFormatting.getValue());
        }
        return rangeMap;
    }

    @Override
    public String toString() {
        return range.toString() + " " + value;
    }

}
