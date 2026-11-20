package com.warrantvault.utils;

import com.warrantvault.models.Product;
import com.warrantvault.models.Warranty;

import java.util.Calendar;
import java.util.Date;

public class WarrantyUtils {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRING = "EXPIRING SOON";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_NO_WARRANTY = "NO WARRANTY";

    /**
     * Determines warranty status, days remaining, and percentage elapsed.
     */
    public static void computeWarrantyMetrics(Product product) {
        if (product == null) return;
        Warranty warranty = product.getWarranty();
        if (warranty == null) {
            product.setStatus(STATUS_EXPIRED);
            product.setDaysRemaining(0);
            product.setElapsedPercentage(100);
            return;
        }

        // Handle "No Warranty" products
        String period = warranty.getWarrantyPeriod();
        if ("No Warranty".equalsIgnoreCase(period) || STATUS_NO_WARRANTY.equals(warranty.getStatus())) {
            product.setStatus(STATUS_NO_WARRANTY);
            product.setDaysRemaining(0);
            product.setElapsedPercentage(0);
            warranty.setStatus(STATUS_NO_WARRANTY);
            return;
        }

        Date today = getZeroTimeDate(new Date());
        Date startDate = getZeroTimeDate(DateUtils.parseDate(warranty.getStartDate()));
        Date endDate = getZeroTimeDate(DateUtils.parseDate(warranty.getEndDate()));

        long totalDays = DateUtils.getDaysBetween(startDate, endDate);
        if (totalDays <= 0) totalDays = 1;

        long daysRemaining = DateUtils.getDaysBetween(today, endDate);
        product.setDaysRemaining(daysRemaining);

        if (today.after(endDate)) {
            product.setStatus(STATUS_EXPIRED);
            product.setDaysRemaining(0);
            product.setElapsedPercentage(100);
            warranty.setStatus(STATUS_EXPIRED);
        } else {
            long daysPassed = DateUtils.getDaysBetween(startDate, today);
            if (daysPassed < 0) daysPassed = 0;
            int percentage = (int) Math.min(100, Math.max(0, (daysPassed * 100) / totalDays));
            product.setElapsedPercentage(percentage);

            if (daysRemaining <= 30) {
                product.setStatus(STATUS_EXPIRING);
                warranty.setStatus(STATUS_EXPIRING);
            } else {
                product.setStatus(STATUS_ACTIVE);
                warranty.setStatus(STATUS_ACTIVE);
            }
        }
    }

    private static Date getZeroTimeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
