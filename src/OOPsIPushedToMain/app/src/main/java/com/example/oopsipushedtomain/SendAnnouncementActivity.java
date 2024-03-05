package com.example.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the page where organizers can create announcements to send to attendees.
 * The announcements are created and uploaded to the database where a Cloud Function notices the
 * new announcement and send a notification to that event topic.
 * @author  Aidan Gironella
 * @version 1.1
 * @see     PushNotificationService
 */
public class SendAnnouncementActivity extends AppCompatActivity {

    private EditText announcementTitleE;
    private EditText announcementBodyE;
    private Button sendAnnouncementButton, cancelButton, subButton, unSubButton;

    private String event;  // TODO: Make it an event ID instead of a string (dependent on database design)
    private CollectionReference announcementsRef;

    /**
     * Initialize the activity by restoring the state if necessary, setting the ContentView,
     * initializing our UI elements, establishing the database connection, and setting our
     * button listeners.
     * @param savedInstanceState If supplied, is used to restore the activity to its prior state.
     *      If not supplied, create a fresh activity.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_announcement);
        event = getIntent().getStringExtra("event");

        // Initialize EditTexts and Buttons
        initializeViews();

        // Initialize database reference to Announcements collection
        announcementsRef = FirebaseFirestore.getInstance().collection("announcements");

        // Set button listeners
        setListeners();
    }

    /**
     * Initialize all the views using findViewById, and set the uneditable Event Title field to
     * the event title.
     */
    private void initializeViews() {
        // Declare UI elements
        EditText eventTitleE = findViewById(R.id.event_title_title);
        announcementTitleE = findViewById(R.id.announcement_title_e);
        announcementBodyE = findViewById(R.id.announcement_body_e);
        sendAnnouncementButton = findViewById(R.id.btnSendNotification);
        cancelButton = findViewById(R.id.btnCancel);
        subButton = findViewById(R.id.btnSub);
        unSubButton = findViewById(R.id.btnUnsub);

        eventTitleE.setText(event);
    }

    /**
     * Set the button listeners.
     * The Send button builds an announcement object and passes it to sendAnnouncement()
     * The cancel button (//TODO maybe unnecessary?) closes this activity
     */
    private void setListeners() {
        sendAnnouncementButton.setOnClickListener(v -> {
            // Retrieve announcement data from the EditTexts
            String title = announcementTitleE.getText().toString();
            String body = announcementBodyE.getText().toString();

            // Check if the organizer created a proper announcement
            if (title.isEmpty() || body.isEmpty()) {
                alertEmptyFields();
                return;
            }

            // Build and send the announcement
            Map<String, Object> announcement = new HashMap<>();
            String topic = "test-notifications";  // TODO: Set this to unique event ID
            announcement.put("image", "image");  // TODO
            announcement.put("title", title);
            announcement.put("body", body);
            announcement.put("eventId", topic);  // TODO: Pass in unique event ID
            Log.d("Announcements", "Sending announcement");
            sendAnnouncement(announcement);
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());

        // Testing button, should be deleted eventually
        subButton.setOnClickListener(v -> FirebaseMessaging.getInstance().subscribeToTopic("test-notifications")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d("Announcement", msg);
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                }));

        // Testing button, should be deleted eventually
        unSubButton.setOnClickListener(v -> FirebaseMessaging.getInstance().unsubscribeFromTopic("test-notifications")
                .addOnCompleteListener(task -> {
                    String msg = "Unsubscribed";
                    if (!task.isSuccessful()) {
                        msg = "Unsubscribe failed";
                    }
                    Log.d("Announcement", msg);
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                }));
    }

    /**
     * Shows an AlertDialog informing the user that they have not provided enough info.
     */
    private void alertEmptyFields() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert
                .setTitle("Missing info")
                .setMessage("One or more fields are empty. Please try again");
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    /**
     * Takes an announcement Map and posts it to the database.
     * @param announcement The announcement Map with all the info to post to the database.
     */
    private void sendAnnouncement(Map<String, Object> announcement) {
        announcementsRef
                .document("uid")  // TODO: UID?
                .set(announcement)
                .addOnSuccessListener(e -> {
                    Log.d("Announcement", "Announcement successfully sent to DB");
                    AlertDialog.Builder alert = new AlertDialog.Builder(getBaseContext());
                    alert
                            .setTitle("Announcement sent")
                            .setMessage("Your announcement was successfully sent!");
                    AlertDialog dialog = alert.create();
                    dialog.show();
                })
                .addOnFailureListener(e -> Log.d("Announcement", "Announcement could not be sent to DB" + e));
    }
}
