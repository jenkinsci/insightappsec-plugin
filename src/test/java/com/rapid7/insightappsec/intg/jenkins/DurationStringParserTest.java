package com.rapid7.insightappsec.intg.jenkins;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DurationStringParserTest {

    @Test
    public void test_parseDurationString_blank() {
        Long fromNull = DurationStringParser.INSTANCE.parseDurationString(null);
        Long fromEmpty = DurationStringParser.INSTANCE.parseDurationString("");
        Long fromBlank = DurationStringParser.INSTANCE.parseDurationString(" ");

        assertEquals(fromNull, null);
        assertEquals(fromEmpty, null);
        assertEquals(fromBlank, null);
    }

    @Test
    public void test_parseDurationString_valid()  {
        String minutesOnly = "0d 0h 10m";
        test(0, 0, 10, minutesOnly);

        String hoursOnly = "0d 10h 0m";
        test(0, 10, 00, hoursOnly);

        String daysOnly = "10d 0h 0m";
        test(10, 0, 0, daysOnly);

        String hoursAndMinutes = "0d 10h 10m";
        test(0, 10, 10, hoursAndMinutes);

        String daysAndMinutes = "10d 0h 10m";
        test(10, 0, 10, daysAndMinutes);

        String daysHoursAndMinutes = "10d 10h 10m";
        test(10, 10, 10, daysHoursAndMinutes);

        String allZeroes = "0d 0h 0m";
        test(0, 0, 0, allZeroes);

        String allSingleDigits = "1d 1h 1m";
        test(1, 1, 1, allSingleDigits);

        String allTripleDigits = "100d 100h 100m";
        test(100, 100, 100, allTripleDigits);

        String mixedDigits = "1d 100h 1000m";
        test(1, 100, 1000, mixedDigits);

        String precedingZeroes = "01d 01h 01m";
        test(1, 1, 1, precedingZeroes);
    }

    private void test(int days,
                      int hours,
                      int minutes,
                      String DurationString) {
        // given
        long actual = DurationStringParser.INSTANCE.parseDurationString(DurationString);

        // when
        long expected = TimeUnit.DAYS.toNanos(days) +
                TimeUnit.HOURS.toNanos(hours) +
                TimeUnit.MINUTES.toNanos(minutes);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void test_parseDurationString_invalid() {
        String noSpace_dayAndHour = "0d0h 10m";
        testException(noSpace_dayAndHour);

        String noSpace_hourAndMinute = "0d 0h10m";
        testException(noSpace_hourAndMinute);

        String noSpaces = "0d0h10m";
        testException(noSpaces);

        String spaceBetweenDayAndUnit = "0 d 0h 10m";
        testException(spaceBetweenDayAndUnit);

        String spaceBetweenHourAndUnit = "0d 0 h 10m";
        testException(spaceBetweenHourAndUnit);

        String spaceBetweenMinuteAndUnit = "0d 0h 10 m";
        testException(spaceBetweenMinuteAndUnit);

        String spaceBetweenQuantityAndUnit = "0 d 0 h 10 m";
        testException(spaceBetweenQuantityAndUnit);

        String precedingSpaces = "  1d 1h 1m";
        testException(precedingSpaces);

//        String followingSpaces = "1d 1h 1m   "; // this actually does work, no reason to force a failure
//        testException(followingSpaces);

        String doubleSpaces = "1d  1h  1m";
        testException(doubleSpaces);

        String missingQuantities = "d h m";
        testException(missingQuantities);

        String missingUnits = "1 1 1";
        testException(missingUnits);
    }

    private void testException(String DurationString) {
        boolean pass = false;
        try {
            DurationStringParser.INSTANCE.parseDurationString(DurationString);
        } catch(Exception e) {
            pass = e.getClass() == IllegalArgumentException.class;
        }
        assertTrue(pass);
    }

}
