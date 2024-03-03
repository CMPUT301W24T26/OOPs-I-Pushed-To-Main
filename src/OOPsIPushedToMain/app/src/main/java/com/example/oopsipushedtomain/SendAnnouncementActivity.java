package com.example.oopsipushedtomain;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class defines the page where organizers can create announcements to send to attendees.
 * @author Aidan Gironella
 * @version 1.0
 */
public class SendAnnouncementActivity extends AppCompatActivity {
    private String event;  // TODO: Make it an event ID instead of a string (dependent on database design)
    private ArrayList<String> attendees;  // TODO: Make it a list of Attendees instead of Strings
    private EditText announcementTitle;
    private EditText announcementBody;
    private Button sendAnnouncementButton;
    private Button cancelButton;
    private String CHANNEL_ID = "test";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_announcement);
        createNotificationChannel();

        announcementTitle = findViewById(R.id.announcement_title_e);
        announcementBody = findViewById(R.id.announcement_body_e);
        sendAnnouncementButton = findViewById(R.id.btnSendNotification);
        cancelButton = findViewById(R.id.btnCancel);

        event = getIntent().getStringExtra("event");

        // TODO: Query database and get a list of attendees using the event ID
        String[] dummyAttendees = {"Adam", "Bill", "Clyde"};
        attendees = new ArrayList<>();
        attendees.addAll(Arrays.asList(dummyAttendees));

        // TODO: Change this intent to open the event that this announcement is for
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendAnnouncementButton.setOnClickListener(v -> {
            Log.d("Announcement", "Sending announcement");
            String title = announcementTitle.getText().toString();
            String body = announcementBody.getText().toString();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)  // TODO: App icon
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
            }
            // TODO: Generate unique notification IDs (potentially in the database?)
            NotificationManagerCompat.from(this).notify(1, builder.build());
            Log.d("Announcement", "Announcement sent?");
        });

        cancelButton.setOnClickListener(v -> {finish();});
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default";
            String description = "Default notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
