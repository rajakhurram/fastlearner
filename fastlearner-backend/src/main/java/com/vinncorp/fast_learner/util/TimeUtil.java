package com.vinncorp.fast_learner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    public static String convertDurationToString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        String durationString = "";

        if (hours > 0) {
            durationString += hours + " hour" + (hours > 1 ? "s" : "") + " ";
        }
        if (minutes > 0) {
            durationString += minutes + " min" + (minutes > 1 ? "s" : "") + " ";
        }
        if (remainingSeconds > 0 || durationString.isEmpty()) {
            durationString += remainingSeconds + " sec" + (remainingSeconds > 1 ? "s" : "");
        }

        return durationString;
    }

    public static int convertDurationToSeconds(String duration) {
        Pattern pattern = Pattern.compile("PT(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?");
        Matcher matcher = pattern.matcher(duration);

        int hours = 0, minutes = 0, seconds = 0;

        if (matcher.matches()) {
            String hourPart = matcher.group(1);
            String minutePart = matcher.group(2);
            String secondPart = matcher.group(3);

            if (hourPart != null) {
                hours = Integer.parseInt(hourPart);
            }
            if (minutePart != null) {
                minutes = Integer.parseInt(minutePart);
            }
            if (secondPart != null) {
                seconds = Integer.parseInt(secondPart);
            }
        }

        return hours * 3600 + minutes * 60 + seconds;
    }

    public static Date convertToDate(String sqlDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            return formatter.parse(sqlDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isWithinOneDay(Date date) {
        if (date == null) {
            return false; // Handle null date
        }

        long oneDayInMillis = 24 * 60 * 60 * 1000L; // One day in milliseconds
        long currentTime = System.currentTimeMillis(); // Current time in milliseconds
        long timeDifference = Math.abs(currentTime - date.getTime());

        return timeDifference <= oneDayInMillis;
    }
}
