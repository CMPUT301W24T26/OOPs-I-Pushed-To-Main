package com.oopsipushedtomain.Announcements;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.R;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
     * The list of announcement
     */
    private ArrayList<Announcement> announcementDataList;

    /**
     * The adapter to show the announcement in the ListView
     */
    private AnnouncementListAdapter announcementListAdapter;

    /**
     * Database access object
     */
    private FirebaseAccess db;

    /**
     * The event ID of the announcement
     */
    private String eventId;

    /**
     * The tag for Log output
     */
    private final String TAG = "AnnouncementListActivity";

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
        ListView announcementList = findViewById(R.id.announcement_list);
        announcementDataList = new ArrayList<>();

        announcementListAdapter = new AnnouncementListAdapter(this, announcementDataList);
        announcementList.setAdapter(announcementListAdapter);

        // Get intent data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        if (eventId != null)
            Log.d(TAG, eventId);

        // Initialize Firestore instance and get the event's announcements
        db = new FirebaseAccess(FirestoreAccessType.EVENTS);
        if (eventId != null)
            getEventAnnouncements();
    }

    /**
     * First gets the event's announcements from the 'announcements' field in 'events', then
     * converts that to an ArrayList that we can use. Next, iterate over the ArrayList and get the
     * individual announcements from the 'announcements' collection.
     */
    private void getEventAnnouncements() {
        db.getAllDocuments(eventId, FirebaseInnerCollection.announcements).thenAccept(announcements -> {
            if (announcements == null) {
                Log.d(TAG, "No announcements found");
            } else {
                Log.d(TAG, "Found announcements");
                runOnUiThread(() -> {
                    announcementDataList.addAll(announcements.stream().map(Announcement::new).collect(Collectors.toList()));
                    announcementListAdapter.notifyDataSetChanged();
                });
            }
        }).exceptionally(e -> {
            Log.e(TAG, "Error getting announcements", e);
            return null;
        });
    }
}