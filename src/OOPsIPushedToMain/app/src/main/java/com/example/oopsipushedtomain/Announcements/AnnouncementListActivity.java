package com.example.oopsipushedtomain.Announcements;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oopsipushedtomain.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AnnouncementListActivity extends AppCompatActivity {
    private ListView announcementList;
    private ArrayList<Announcement> announcementDataList;
    private AnnouncementListAdapter announcementListAdapter;
    private FirebaseFirestore db;
    private CollectionReference announcementsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_list);

        announcementList = findViewById(R.id.announcement_list);
        announcementDataList = new ArrayList<>();

        announcementListAdapter = new AnnouncementListAdapter(this, announcementDataList);
        announcementList.setAdapter(announcementListAdapter);

        db = FirebaseFirestore.getInstance();
        announcementsRef = db.collection("announcements");
        addListeners();
    }

    /**
     * Adds listeners for the activity
     */
    private void addListeners() {

        // TODO: Get announcements for that user (anmtId in the user collection maybe?)
        announcementsRef.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e("Firebase", error.toString());
                return;
            }
            if (queryDocumentSnapshots != null) {
                announcementDataList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String title = (String) doc.getData().get("title");
                    String body = (String) doc.getData().get("body");
                    String imageId = (String) doc.getData().get("imageId");
                    String eventId = (String) doc.getData().get("eventId");
                    announcementDataList.add(new Announcement(title, body, imageId, eventId));
                }
                announcementListAdapter.notifyDataSetChanged();
            }
        });

        announcementList.setOnItemClickListener((adapterView, view, position, id) -> {
            Announcement announcement = announcementDataList.get(position);
            view.setSelected(true);

            // TODO: Integration - This should properly open the event details for the
            //  announcement's event so try just uncommenting this. Might need to mess
            //  with the nesting for EventDetailsActivityAttendee.class
//                Intent i = new Intent(getBaseContext(), EventDetailsActivityAttendee.class);
//                Event event = db.collection("events").whereEqualTo("eventId", announcement.getEventId());
//                i.putExtra("selectedEvent", event);
//                startActivity(i);
        });
    }
}