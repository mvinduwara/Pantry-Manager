package com.example.snaplog.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.snaplog.R;
import com.example.snaplog.activity.InventoryActivity;
import com.example.snaplog.database.AppDatabase;
import com.example.snaplog.database.PantryItem;

import java.util.List;

public class PantryWorker extends Worker {

    private static final String CHANNEL_ID = "pantry_alerts";

    public PantryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<PantryItem> allItems = db.pantryItemDao().getAllItems();

        long currentTime = System.currentTimeMillis();
        long twoDaysFromNow = currentTime + (2L * 24 * 60 * 60 * 1000);

        int expiringCount = 0;
        for (PantryItem item : allItems) {
            if (item.expirationDate <= twoDaysFromNow) {
                expiringCount++;
            }
        }

        if (expiringCount > 0) {
            sendNotification(expiringCount);
        }

        return Result.success();
    }

    private void sendNotification(int count) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Pantry Expiration Alerts", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Make the notification open the Inventory screen when tapped
        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Built-in warning icon
                .setContentTitle("Items Expiring Soon!")
                .setContentText("You have " + count + " item(s) expiring soon. Tap to ask Virtual Chef for a recipe.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}