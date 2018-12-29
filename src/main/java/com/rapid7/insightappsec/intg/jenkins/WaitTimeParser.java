package com.rapid7.insightappsec.intg.jenkins;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

public class WaitTimeParser {

    private WaitTimeParser() {
        // private constructor
    }

    public static long parseWaitTimeString(String waitTimeString) {
        if (StringUtils.isBlank(waitTimeString)) {
            return -1L;
        }

        try {
            int dayIndex = waitTimeString.indexOf("d");
            int hourIndex = waitTimeString.indexOf("h");
            int minuteIndex = waitTimeString.indexOf("m");

            // units are present
            if (dayIndex == -1 || hourIndex == -1 || minuteIndex == -1) {
                throw new IllegalArgumentException();
            }

            // space after unit
            if (waitTimeString.charAt(dayIndex + 1) != ' ' || waitTimeString.charAt(hourIndex + 1) != ' ') {
                throw new IllegalArgumentException();
            }

            // quantities are present - will throw number format exe
            int dayQuantity = Integer.parseInt(waitTimeString.substring(0, dayIndex));
            int hourQuantity = Integer.parseInt(waitTimeString.substring(dayIndex + 2, hourIndex));
            int minuteQuantity = Integer.parseInt(waitTimeString.substring(hourIndex + 2, minuteIndex));

            return TimeUnit.DAYS.toNanos(dayQuantity) +
                   TimeUnit.HOURS.toNanos(hourQuantity) +
                   TimeUnit.MINUTES.toNanos(minuteQuantity);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
