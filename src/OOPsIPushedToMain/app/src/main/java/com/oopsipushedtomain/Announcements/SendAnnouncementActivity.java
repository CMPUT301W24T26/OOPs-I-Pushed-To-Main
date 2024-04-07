package com.oopsipushedtomain.Announcements;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
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
     * The UID of the event to send this announcement for
     */
    private String eventId;

    /**
     * Custom database access class
     */
    private FirebaseAccess db;

    /**
     * Tag for logging
     */
    private final String TAG = "SendAnnouncementActivity";

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

        // Initialize database for Events collection
        db = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Initialize EditTexts and Buttons
        initializeViews();

        // Get the event info
        getEventInfo();

        // Set button listeners
        setListeners();
    }

    /**
     * This method receives an event ID from the Intent, and uses it to set eventRef. The database
     * is then queried to find the event in the 'events' collection. Uses the event title it
     * finds in the database to get the (uneditable) announcementTitleE EditText.
     */
    private void getEventInfo() {
        eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            Log.d(TAG, eventId);
        }

        db.getDataFromFirestore(eventId).thenAccept(event -> {
            if (event == null) {
                Log.e(TAG, "Could not retrieve info for event");
            } else {
                Log.d(TAG, "Found data for event");
                runOnUiThread(() -> eventTitleE.setText((String) event.get("title")));
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
     * The cancel button closes this activity
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

            // Build and store the announcement in the database
            Map<String, Object> announcement = new HashMap<>();
            announcement.put("title", title);
            announcement.put("body", body);
            announcement.put("imageId", "image");
            announcement.put("eventId", eventId);
            Log.d("Announcements", "Sending announcement");
            db.storeDataInFirestore(eventId, FirebaseInnerCollection.announcements, null, announcement);
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
}
