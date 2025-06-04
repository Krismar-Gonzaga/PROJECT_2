package com.example.myapp;

import static android.content.Intent.getIntent;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "low_stock_channel";
    private static final String CHANNEL_NAME = "Low Stock Notifications";
    private static final int NOTIFICATION_ID = 1;


    @SuppressLint("NotificationPermission")
    public static void showLowStockNotification(Context context, int lowStockCount, User currentUser,OnMenuVisibility MenuVisibility) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent that will always go to homeactivity
        Intent intent = new Intent(context, homeactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("OPEN_FRAGMENT", "notification");
        intent.putExtra("CURRENT_USER", currentUser);
        intent.putExtra("low_stock_count", lowStockCount);
        MenuVisibility.UpdateMenuVisibility();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Low Stock Alert")
                .setContentText(lowStockCount + " items are low in stock")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}