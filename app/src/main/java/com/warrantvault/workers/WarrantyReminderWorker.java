package com.warrantvault.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.warrantvault.dao.NotificationDAO;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.models.NotificationItem;
import com.warrantvault.models.Product;
import com.warrantvault.utils.DateUtils;
import com.warrantvault.utils.NotificationUtils;
import com.warrantvault.utils.SessionManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarrantyReminderWorker extends Worker {

    public static final String WORK_NAME = "warranty_vault_reminder_work";

    public WarrantyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isNotificationsEnabled() || !sessionManager.isLoggedIn()) {
            return Result.success();
        }

        long userId = sessionManager.getUserId();
        ProductDAO productDAO = new ProductDAO(context);
        NotificationDAO notificationDAO = new NotificationDAO(context);

        List<Product> products = productDAO.getAllProducts(userId);
        for (Product product : products) {
            long days = product.getDaysRemaining();

            boolean shouldNotify = false;
            if (days == 30 && sessionManager.isRemind30()) {
                shouldNotify = true;
            } else if (days == 14 && sessionManager.isRemind14()) {
                shouldNotify = true;
            } else if (days == 7 && sessionManager.isRemind7()) {
                shouldNotify = true;
            } else if (days == 1 && sessionManager.isRemind1()) {
                shouldNotify = true;
            } else if (days <= 0) {
                // Expired notification if not already sent
                shouldNotify = (days == 0);
            }

            if (shouldNotify) {
                String title = (days <= 0) ? "Warranty Expired" : "Warranty Expiring Soon";
                String message = (days <= 0)
                        ? "Your " + product.getName() + " warranty has expired."
                        : "Your " + product.getName() + " warranty expires in " + days + " days.";

                // 1. Show Android system notification
                NotificationUtils.showNotification(context, (int) product.getId(), product.getId(), title, message);

                // 2. Persist in local notifications table
                NotificationItem item = new NotificationItem(
                        product.getId(),
                        title,
                        message,
                        DateUtils.getTodayDisplay(),
                        false
                );
                notificationDAO.insertNotification(item);
            }
        }

        return Result.success();
    }

    public static void schedulePeriodicReminders(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WarrantyReminderWorker.class,
                24, TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
