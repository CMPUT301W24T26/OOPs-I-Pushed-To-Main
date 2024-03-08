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
import com.oopsipushedtomain.EventDetailsActivity;
import com.oopsipushedtomain.R;

import java.util.ArrayList;

/**
 * This activity obtains and displays a list of announcements sent for that even. The eventId is
 * passed to this activity through an Intent, which is then used to get a list of their
 * announcements via the 'announcements' field in the 'events' collection. The 'announcements'
 * collection is then queried using this list to get all the announcement details.
 * @author  Aidan Gironella
 * @see     Announcement
 * @see     AnnouncementListAdapter
 */
public class AnnouncementListActivity extends AppCompatActivity {
    private ListView announcementList;
    private ArrayList<Announcement> announcementDataList;
    private AnnouncementListAdapter announcementListAdapter;
    private FirebaseFirestore db;
    private DocumentReference announcementRef;
    private Event selectedEvent;
    private String eventId;
    private ArrayList<String> announcements;
    private final String TAG = "EventAnnouncements";

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

        // Initialize Firestore instance and get the event's announcements
        db = FirebaseFirestore.getInstance();
        getEventAnnouncements();
        addListeners();
    }

    /**
     * Adds listeners for the activity
     */
    private void addListeners() {

        announcementList.setOnItemClickListener((adapterView, view, position, id) -> {
            Announcement announcement = announcementDataList.get(position);
            view.setSelected(true);

            // TODO: Integration - This should properly open the event details for the
            //  announcement's event so try just uncommenting this. Might need to mess
            //  with the nesting for EventDetailsActivityAttendee.class
            Intent i = new Intent(getBaseContext(), EventDetailsActivity.class);
            Event selectedEvent = getSelectedEvent(announcement.getEventId());
            i.putExtra("selectedEvent", selectedEvent);
            startActivity(i);
        });
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
                    for (String announcement: announcements) {
                        Log.d(TAG, announcement);

                        // Take the announcement UID and find that announcement in the
                        // 'announcements' collection
                        announcementRef = db.collection("announcements").document(announcement);
                        announcementRef.get().addOnCompleteListener(getAnnouncementTask -> {
                            if (getAnnouncementTask.isSuccessful()) {
                                DocumentSnapshot announcementDoc = getAnnouncementTask.getResult();
                                if (announcementDoc.exists()) {  // The announcement was successfully found
                                    announcementDataList.add(announcementDoc.toObject(Announcement.class));
                                    announcementListAdapter.notifyDataSetChanged();
                                } else {
                                    Log.e(TAG,
                                            String.format("Could not find announcement %s for event %s",
                                                    announcementDoc, eventId));
                                }
                            } else {
                                Log.e(TAG, "Get individual announcement task failed, ",
                                        getAnnouncementTask.getException());
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Could not find event's announcements");
                }
            } else {
                Log.e(TAG, "Get event announcements task failed, ",
                        getEventTask.getException());
            }
        });
    }

    private Event getSelectedEvent(String selectedEventId) {
        DocumentReference eventRef = db.collection("events").document(selectedEventId);
        eventRef.get().addOnCompleteListener(getEventTask -> {
            if (getEventTask.isSuccessful()) {
                DocumentSnapshot eventDoc = getEventTask.getResult();
                if (eventDoc.exists()) {  // The announcement was successfully found
                    selectedEvent = eventDoc.toObject(Event.class);
                } else {
                    Log.e(TAG,
                            String.format("Could not find event %s", eventDoc));
                }
            } else {
                Log.e(TAG, "Get individual event task failed, ",
                        getEventTask.getException());
            }
        });
        return selectedEvent;
    }
}