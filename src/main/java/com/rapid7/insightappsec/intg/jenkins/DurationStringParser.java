package com.rapid7.insightappsec.intg.jenkins;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

public class DurationStringParser {

    private static final int NON_EXISTENT_STRING_INDEX = -1;

    private DurationStringParser() {
        // private constructor
    }

    public static Long parseDurationString(String durationString) {
        if (StringUtils.isBlank(durationString)) {
            return null;
        }

        try {
            int dayIndex = durationString.indexOf("d");
            int hourIndex = durationString.indexOf("h");
            int minuteIndex = durationString.indexOf("m");

            // units are present
            if (dayIndex == NON_EXISTENT_STRING_INDEX ||
                hourIndex == NON_EXISTENT_STRING_INDEX ||
                minuteIndex == NON_EXISTENT_STRING_INDEX) {
                throw new IllegalArgumentException();
            }

            // space after unit
            if (durationString.charAt(dayIndex + 1) != ' ' || durationString.charAt(hourIndex + 1) != ' ') {
                throw new IllegalArgumentException();
            }

            // quantities are present - will throw number format exe
            int dayQuantity = Integer.parseInt(durationString.substring(0, dayIndex));
            int hourQuantity = Integer.parseInt(durationString.substring(dayIndex + 2, hourIndex));
            int minuteQuantity = Integer.parseInt(durationString.substring(hourIndex + 2, minuteIndex));

            return TimeUnit.DAYS.toNanos(dayQuantity) +
                   TimeUnit.HOURS.toNanos(hourQuantity) +
                   TimeUnit.MINUTES.toNanos(minuteQuantity);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
