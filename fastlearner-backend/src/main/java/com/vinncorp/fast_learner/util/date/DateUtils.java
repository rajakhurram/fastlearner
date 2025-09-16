package com.vinncorp.fast_learner.util.date;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date addMonthsToDate(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }
    public static Date addMonthsToLocalDate(LocalDateTime dateTime, int months) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        if (months < 0) {
            throw new IllegalArgumentException("Months cannot be negative");
        }

        // Convert LocalDateTime to java.util.Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTime.getYear(), dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

        // Add the specified number of months
        calendar.add(Calendar.MONTH, months);

        // Return the updated Date
        return calendar.getTime();
    }
    public static Date addYearsToDate(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    public static String showDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        return formatter.format(date);
    }
}
