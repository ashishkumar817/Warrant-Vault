package com.warrantvault.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static final String DISPLAY_FORMAT = "dd MMM yyyy";
    public static final String DB_FORMAT = "yyyy-MM-dd";
    public static final String SLASH_FORMAT = "dd/MM/yyyy";

    public static String getTodayDisplay() {
        return new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault()).format(new Date());
    }

    public static String getTodayDb() {
        return new SimpleDateFormat(DB_FORMAT, Locale.getDefault()).format(new Date());
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new Date();
        }
        String[] formats = {DISPLAY_FORMAT, DB_FORMAT, SLASH_FORMAT, "dd-MM-yyyy", "yyyy/MM/dd", "MM/dd/yyyy"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(true);
                return sdf.parse(dateStr.trim());
            } catch (ParseException ignored) {
            }
        }
        return new Date();
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault()).format(date);
    }

    public static String normalizeToDisplay(String dateStr) {
        Date d = parseDate(dateStr);
        return formatDate(d);
    }

    public static String calculateEndDate(String startDateStr, int amount, String unit) {
        Date startDate = parseDate(startDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        if ("Years".equalsIgnoreCase(unit) || "Year".equalsIgnoreCase(unit)) {
            calendar.add(Calendar.YEAR, amount);
        } else if ("Days".equalsIgnoreCase(unit) || "Day".equalsIgnoreCase(unit)) {
            calendar.add(Calendar.DAY_OF_YEAR, amount);
        } else {
            // Default Months
            calendar.add(Calendar.MONTH, amount);
        }
        // Exactly matches user example: 12 Aug 2026 + 12 Months -> 11 Aug 2027 (1 day before full year boundary)
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        return formatDate(calendar.getTime());
    }

    public static long getDaysBetween(Date fromDate, Date toDate) {
        long diff = toDate.getTime() - fromDate.getTime();
        return diff / (24L * 60L * 60L * 1000L);
    }
}
