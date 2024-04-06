package com.oopsipushedtomain.Announcements;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oopsipushedtomain.R;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the page where organizers can create announcements to send to attendees.
 * The announcements are created and uploaded to the database where a Cloud Function notices the
 * new announcement and send a notification to that event topic.
 * @author  Aidan Gironella
 * @see     PushNotificationService
 */
public class SendAnnouncementActivity extends AppCompatActivity {

    /**
     * The view for the announcement title or body
     */
    private EditText announcementTitleE, announcementBodyE;
    /**
     * The view for the event title
     */
    private TextView eventTitleE;
    /**
     * The button references for the send or cancel button
     */
    private Button sendAnnouncementButton, cancelButton;

    /**
     * The event title or id
     */
    private String eventTitle, eventId;
    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db;
    /**
     * The reference to the collection for announcement
     */
    private CollectionReference announcementsRef;

    /**
     * A reference to the event document for the announcement
     */
    private DocumentReference eventRef;
    /**
     * Tag for logging
     */
    private final String TAG = "SendAnnouncement";

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

        // Initialize database reference to Announcements collection
        db = FirebaseFirestore.getInstance();
        announcementsRef = db.collection("announcements");

        // Initialize EditTexts and Buttons
        initializeViews();

        // Get the event info
        getEvent();

        // Set button listeners
        setListeners();
    }

    /**
     * This method receives an event ID from the Intent, and uses it to set eventRef. The database
     * is then queried to find the event in the 'events' collection. Uses the event title it
     * finds in the database to get the (uneditable) announcementTitleE EditText.
     */
    private void getEvent() {
        eventId = getIntent().getStringExtra("eventId");
        Log.d(TAG, eventId);

        // Get the event from the database
        eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(getEventTask -> {
            if (getEventTask.isSuccessful()) {
                DocumentSnapshot eventDoc = getEventTask.getResult();
                if (eventDoc.exists()) {  // The event was successfully found
                    eventTitle = eventDoc.getString("title");
                    Log.d(TAG, String.format("Found event %s", eventTitle));
                    eventTitleE.setText(eventTitle);

                    // TODO: used for testing, this should actually be called when attendees
                    //  sign up for events but that doesn't exist yet so this is a stopgap
//                    FirebaseMessaging.getInstance().subscribeToTopic(eventDoc.getId());
//                    Log.e(TAG, "Subscribed to topic " + eventDoc.getId());
                } else {
                    Log.e(TAG,
                            String.format("Could not find event %s", eventId));
                }
            } else {
                Log.e(TAG, "Get individual event task failed, ",
                        getEventTask.getException());
            }
        });
    }

    /**
     * Initialize all the views using findViewById, and set the uneditable Event Title field to
     * the event title.
     */
    private void initializeViews() {
        // Declare UI elements
        eventTitleE = findViewById(R.id.event_title_title);
        announcementTitleE = findViewById(R.id.announcement_title_e);
        announcementBodyE = findViewById(R.id.announcement_body_e);
        sendAnnouncementButton = findViewById(R.id.btnSendNotification);
        cancelButton = findViewById(R.id.btnCancel);
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
            announcement.put("eventId", eventId);
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
        eventRef.update("announcements", FieldValue.arrayUnion("ANMT-" + uid));
    }
}
