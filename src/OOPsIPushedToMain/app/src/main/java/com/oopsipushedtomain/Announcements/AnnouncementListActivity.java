package com.oopsipushedtomain.Announcements;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oopsipushedtomain.Event;
import com.oopsipushedtomain.R;

import java.util.ArrayList;

/**
 * This activity obtains and displays a list of announcements sent for that even. The eventId is
 * passed to this activity through an Intent, which is then used to get a list of their
 * announcements via the 'announcements' field in the 'events' collection. The 'announcements'
 * collection is then queried using this list to get all the announcement details.
 *
 * @author Aidan Gironella
 * @see Announcement
 * @see AnnouncementListAdapter
 */
public class AnnouncementListActivity extends AppCompatActivity {
    /**
     * The view that shows the list of announcements
     */
    private ListView announcementList;

    /**
     * The list of announcement
     */
    private ArrayList<Announcement> announcementDataList;

    /**
     * The adapter to show the announcement in the ListView
     */
    private AnnouncementListAdapter announcementListAdapter;

    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db;

    /**
     * A reference to the announcements document
     */
    private DocumentReference announcementRef;

    /**
     * The event ID of the announcement
     */
    private String eventId;

    /**
     * An list of all the announcement stored as strings
     */
    private ArrayList<String> announcements;

    /**
     * The tag for Log output
     */
    private final String TAG = "EventAnnouncements";

    /**
     * Creates and instantiates the activity.
     * Finds the views and sets the adapter for the ListView
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it
     *                           most recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_list);

        // Initialize the list and list adapter
        announcementList = findViewById(R.id.announcement_list);
        announcementDataList = new ArrayList<>();

        announcementListAdapter = new AnnouncementListAdapter(this, announcementDataList);
        announcementList.setAdapter(announcementListAdapter);

        // Get intent data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        Log.d(TAG, eventId);

        // Initialize Firestore instance and get the event's announcements
        db = FirebaseFirestore.getInstance();
        getEventAnnouncements();
    }

    /**
     * First gets the event's announcements from the 'announcements' field in 'events', then
     * converts that to an ArrayList that we can use. Next, iterate over the ArrayList and get the
     * individual announcements from the 'announcements' collection.
     */
    private void getEventAnnouncements() {
        // Find the event in the 'events' collection
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(getEventTask -> {
            if (getEventTask.isSuccessful()) {
                DocumentSnapshot eventDocument = getEventTask.getResult();
                if (eventDocument.exists()) {  // This means we successfully found the event
                    Log.d(TAG, "Found event document");

                    // Get the array of 'announcements' from the 'event' document
                    announcements = (ArrayList<String>) eventDocument.get("announcements");
                    announcementListAdapter.clear();

                    // Iterate over the announcements we found for that event
                    if (announcements == null) {
                        Log.d(TAG, "No announcements to show");
                    } else {
                        for (String announcement : announcements) {
                            Log.d(TAG, announcement);

                            // Take the announcement UID and find that announcement in the
                            // 'announcements' collection
                            announcementRef = db.collection("announcements").document(announcement);
                            announcementRef.get().addOnCompleteListener(getAnnouncementTask -> {
                                if (getAnnouncementTask.isSuccessful()) {
                                    DocumentSnapshot announcementDoc = getAnnouncementTask.getResult();
                                    if (announcementDoc.exists()) {  // The announcement was successfully found
                                        Announcement newAnnouncement = announcementDoc.toObject(Announcement.class);
                                        newAnnouncement.setAnmtId(announcementDoc.getId());
                                        announcementDataList.add(newAnnouncement);
                                        announcementListAdapter.notifyDataSetChanged();
                                        Log.d("test", getFirstAnnouncement());
                                    } else {
                                        Log.e(TAG, String.format("Could not find announcement %s for event %s", announcementDoc, eventId));
                                    }
                                } else {
                                    Log.e(TAG, "Get individual announcement task failed, ", getAnnouncementTask.getException());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "Could not find event's announcements");
                }
            } else {
                Log.e(TAG, "Get event announcements task failed, ", getEventTask.getException());
            }
        });
    }

    /**
     * Get the first announcement in the list. Used for intent testing.
     * @return UID of first announcement in list.
     */
    public String getFirstAnnouncement() {
        return announcementDataList.get(0).getAnmtId();
    }
}