package at.shorty.polar.addon.util;

import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class TimeRange {

    public long start;
    public long end;

    public String formatStart(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(start));
    }

    public String formatEnd(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(end));
    }

    public static TimeRange parseFromString(String string) throws IllegalArgumentException {
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
        Matcher simpleMatcher = Pattern.compile("^(\\d+)([hdwm])$").matcher(string);
        Matcher dateMatcher = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$").matcher(string);
        Matcher rangeMatcher = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2});(\\d{4})-(\\d{2})-(\\d{2})$").matcher(string);
        Matcher monthMatcher = Pattern.compile("^(\\d{4})-(\\d{2})$").matcher(string);
        if (simpleMatcher.matches()) { // 1h, 1d, 1w, 1m
            int duration = Integer.parseInt(simpleMatcher.group(1));
            String unit = simpleMatcher.group(2);
            Duration durationObject = Duration.ZERO;
            switch (unit) {
                case "h":
                    durationObject = Duration.ofHours(duration);
                    break;
                case "d":
                    durationObject = Duration.ofDays(duration);
                    break;
                case "w":
                    durationObject = Duration.ofDays(duration * 7L);
                    break;
                case "m":
                    durationObject = Duration.ofDays(duration * 30L);
                    break;
            }
            Instant now = Instant.now();
            return new TimeRange(now.minus(durationObject).toEpochMilli(), now.toEpochMilli());
        } else if (dateMatcher.matches()) { // yyyy-mm-dd
            int year = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            int day = Integer.parseInt(dateMatcher.group(3));
            Instant start = LocalDate.of(year, month, day).atStartOfDay().toInstant(zoneOffset);
            Instant end = start.plus(Duration.ofDays(1));
            return new TimeRange(start.toEpochMilli(), end.toEpochMilli());
        } else if (monthMatcher.matches()) { // yyyy-mm
            int year = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            Instant start = LocalDate.of(year, month, 1).atStartOfDay().toInstant(zoneOffset);
            Instant end = LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay().toInstant(zoneOffset);
            return new TimeRange(start.toEpochMilli(), end.toEpochMilli());
        } else if (rangeMatcher.matches()) { // yyyy-mm-dd;yyyy-mm-dd
            int yearStart = Integer.parseInt(rangeMatcher.group(1));
            int monthStart = Integer.parseInt(rangeMatcher.group(2));
            int dayStart = Integer.parseInt(rangeMatcher.group(3));
            int yearEnd = Integer.parseInt(rangeMatcher.group(4));
            int monthEnd = Integer.parseInt(rangeMatcher.group(5));
            int dayEnd = Integer.parseInt(rangeMatcher.group(6));
            Instant start = LocalDate.of(yearStart, monthStart, dayStart).atStartOfDay().toInstant(zoneOffset);
            Instant end = LocalDate.of(yearEnd, monthEnd, dayEnd).atStartOfDay().toInstant(zoneOffset).plus(Duration.ofDays(1));
            return new TimeRange(start.toEpochMilli(), end.toEpochMilli());
        } else if (string.equals("today")) { // today
            LocalDate today = LocalDate.now();
            Instant start = today.atStartOfDay().toInstant(zoneOffset);
            Instant end = start.plus(Duration.ofDays(1));
            return new TimeRange(start.toEpochMilli(), end.toEpochMilli());
        } else if (string.equals("yesterday")) { // yesterday
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Instant start = yesterday.atStartOfDay().toInstant(zoneOffset);
            Instant end = start.plus(Duration.ofDays(1));
            return new TimeRange(start.toEpochMilli(), end.toEpochMilli());
        } else {
            throw new IllegalArgumentException("Invalid time range: " + string);
        }
    }

}
