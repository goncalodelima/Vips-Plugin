package com.minecraftsolutions.vip.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
public enum TimeUtils {

    DAY(86400000),
    HOUR(3600000),
    MINUTE(60000),
    SECOND(1000);

    private final long millis;

    private static final String[] formats = {"day", "hour", "minute", "second"};

    private static final Pattern pattern = Pattern.compile("(\\d+)(\\s+)?([a-zA-Z]+)");

    TimeUtils(long millis) {
        this.millis = millis;
    }

    public static String format(long value) {

        if (value == -1) return "Permanent";

        if (value <= 0) return "In a moment";

        long days = TimeUnit.MILLISECONDS.toDays(value);
        long hours = TimeUnit.MILLISECONDS.toHours(value) - (days * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(value) - (TimeUnit.MILLISECONDS.toHours(value) * 60);
        long second = TimeUnit.MILLISECONDS.toSeconds(value) - (TimeUnit.MILLISECONDS.toMinutes(value) * 60);

        long[] times = {days, hours, minutes, second};

        List<String> values = new ArrayList<>();
        for (int index = 0; index < times.length; index++) {
            long time = times[index];
            if (time > 0) {
                String name = times[index] + " " + formats[index] + (time > 1 ? "s" : "");
                values.add(name);
            }
        }

        if (values.isEmpty()) {
            return "In a moment";
        }

        if (values.size() == 1) {
            return values.get(0);
        }

        return String.join(", ", values.subList(0, values.size() - 1)) + " e " + values.get(values.size() - 1);
    }

}