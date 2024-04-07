package com.oopsipushedtomain.Announcements;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.oopsipushedtomain.R;

import java.util.Objects;
import java.util.Random;

/**
 * This service handles incoming push notifications for the app. The app uses Firebase for
 * notifications, which are received as messages, and this service parses message and turns them
 * into Android notifications which are sent to the user.
 * @author  Aidan Gironella
 * @see     SendAnnouncementActivity
 */
public class PushNotificationService extends FirebaseMessagingService {
    /**
     * Tag for logging data
     */
    private final String CHANNEL_ID = "Event Announcements";

    /**
     * Observe token changes for the Firebase project.
     * @param token The token used for sending messages to this application instance. This token is
     *     the same as the one retrieved by {@link FirebaseMessaging#getToken()}.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", token);  // TODO: Store the token somewhere better (database)
    }

    /**
     * Create and show an Android notification when a message is received.
     * @param message Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.e("Notification", "Notification received!");
        super.onMessageReceived(message);

        // Set up the notification channel to listen to
        createNotificationChannel();

        // Show the notification when we receive it
        String title = Objects.requireNonNull(message.getNotification()).getTitle();
        String body = message.getNotification().getBody();
        showNotification(this, title, body);
    }

    /**
     * Creates and sets up the notification channel so that our app can receive messages from
     * Firebase.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Announcements";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription("Announcements sent by organizers for events");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    /**
     * Build an Android notification and send it to the user.
     * @param context The Context of the service
     * @param title A String representing the notification title
     * @param body A String representing the notification body/main text
     */
    public void showNotification(Context context, String title, String body) {
        // Construct a notification object using the passed-in info
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // TODO: App logo
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Set a BigTextStyle for the notification
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(body)
                .setBigContentTitle(title)
                .setSummaryText(title);

        notification.setStyle(bigTextStyle);
        notification.setChannelId(CHANNEL_ID);

        // Check if the user has enabled notifications for the app
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Notifications", "User has notifications disabled");
        }
        NotificationManagerCompat.from(this).notify(new Random().nextInt(), notification.build());
    }
}
