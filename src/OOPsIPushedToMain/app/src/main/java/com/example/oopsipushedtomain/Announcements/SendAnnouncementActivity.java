package com.example.oopsipushedtomain.Announcements;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oopsipushedtomain.R;
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
    private Button sendAnnouncementButton, cancelButton;
    private String eventId;
    private FirebaseFirestore db;
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

        // TODO: Integration - in Ningze EventDetailsActivityOrganizer.java, on line 108 (or inside
        //  btnSendNotification setOnClickListener, before startActivity), add this line:
        //  intent.putExtra("eventId", event.getEventId());
        eventId = getIntent().getStringExtra("eventId");

        // TODO: Integration - and remove this line
        eventId = "EVNT-0000000000";

        // Initialize database reference to Announcements collection
        db = FirebaseFirestore.getInstance();
        announcementsRef = db.collection("announcements");

        // Initialize EditTexts and Buttons
        initializeViews();

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

        // TODO: Integration - this should properly get the event title and fill the field once
        //  Event.java is in the project, try just uncommenting these two lines and remove the third
//        Event event = db.collection("events").whereEqualTo("eventId", eventId)
//        eventTitleE.setText(event.getTitle());
        eventTitleE.setText("Event Name");
    }

    /**
     * Set the button listeners.
     * The Send button builds an announcement object and passes it to sendAnnouncement()
     * The cancel button closes this activity (TODO maybe unnecessary? Feel free to remove)
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
            announcement.put("title", title);
            announcement.put("body", body);
            announcement.put("imageId", "image");
            announcement.put("eventId", "test-notifications");
            Log.d("Announcements", "Sending announcement");
            sendAnnouncement(announcement);
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    /**
     * Shows an AlertDialog informing the user that they have not provided enough info.
     */
    private void alertEmptyFields() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert
                .setTitle("Missing info")
                .setMessage("One or more fields are empty. Please try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    /**
     * Takes an announcement Map and posts it to the database.
     * Uses Firestore to generate announcement UIDs
     * @param announcement The announcement Map with all the info to post to the database.
     */
    private void sendAnnouncement(Map<String, Object> announcement) {
        // Generate a unique ID using Firestore
        String uid = announcementsRef.document().getId().toUpperCase();
        Log.d("Announcement", "ANMT-" + uid);

        announcementsRef
                .document("ANMT-" + uid)
                .set(announcement)
                .addOnSuccessListener(e -> {
                    Log.d("Announcement", "Announcement successfully sent to DB");
                    Toast.makeText(getBaseContext(), "Success, your announcement was sent!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("Announcement", "Announcement could not be sent to DB" + e);
                    Toast.makeText(getBaseContext(), "Error: Your announcement could not be sent", Toast.LENGTH_LONG).show();
                });
    }
}
