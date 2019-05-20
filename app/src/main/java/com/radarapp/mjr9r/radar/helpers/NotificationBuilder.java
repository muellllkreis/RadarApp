package com.radarapp.mjr9r.radar.helpers;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.radarapp.mjr9r.radar.R;

import java.util.UUID;

public class NotificationBuilder {
    public static void buildNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , "RADAR_NOTIFY")
                .setSmallIcon(R.drawable.ic_radar_icon)
                .setContentTitle("New Messages Nearby")
                .setContentText("Someone dropped a message nearby!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Someone dropped a message nearby! Check it out now!"))
                //.setLargeIcon(BitmapFactory.decodeResource(dm.getFilter().getIconID()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(UUID.randomUUID().hashCode(), builder.build());
    }
}
