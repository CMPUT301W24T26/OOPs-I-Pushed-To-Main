package com.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * EventListActivity is responsible for displaying a list of events to the user.
 * It acts as the entry point for users to view events, and upon selection, provides detailed information
 * about the selected event. This activity supports different actions based on the user's role (attendee or organizer).
 *
 * Outstanding issues:
 * 1. The user role check is currently hardcoded to always true, which does not reflect the actual user role.
 *    This needs to be replaced with a dynamic check to determine if the user is an attendee or an organizer.
 * 2. The deletion logic in EventDetailsActivityOrganizer does not currently update the event list in this activity.
 *    A mechanism to refresh the event list after an event is deleted needs to be implemented.
 */

public class EventListActivity extends AppCompatActivity {

    private ListView eventList;
    private ArrayList<Event> eventDataList;
    private ArrayAdapter<Event> eventAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        initializeViews();

        setupListeners();
    }

    private void initializeViews() {
        eventDataList = new ArrayList<>();
        eventList = findViewById(R.id.EventListView);
        eventAdapter = new EventListAdapter(eventDataList, this);
        eventList.setAdapter(eventAdapter);
    }

    private void setupListeners() {
        // Create Event button functionality
        Button createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to the event creation activity
                Intent intent = new Intent(EventListActivity.this, NewEventActivity.class);
                startActivity(intent);
            }
        });

        // Sort Events button functionality
        Button sortButton = findViewById(R.id.sort_events_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EventListActivity.this, v);
                // Inflate the popup menu from a menu resource
                popup.getMenuInflater().inflate(R.menu.event_sort_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle menu item clicks here
                        int itemId = item.getItemId();
                        if (itemId == R.id.sort_by_all_events) {
                            // TODO: Handle sorting given the users corrsponding events
                        } else if (itemId == R.id.sort_by_signed_up) {

                        } else if (itemId == R.id.sort_by_user_event) {

                        }
                        // Optionally, update your ListView adapter to reflect the sorting
                        return true;
                    }
                });
                popup.show(); // Show the popup menu
            }
        });

        // Click on an event in the list functionality
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) parent.getItemAtPosition(position);
                Intent intent;

                // TODO: Replace the following condition with actual logic to determine user role
                if (true) {
                    intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                } else {
                    intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                }

                intent.putExtra("selectedEvent", selectedEvent);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventDataList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String startDateTime = document.getString("startDateTime");
                    String endDateTime = document.getString("endDateTime");
                    String details = document.getString("details");
                    String eventId = document.getId();
                    // Extract other fields as necessary and create a new Event object
                    Event event = new Event(eventId, title, startDateTime, endDateTime, details, "", "", 0);
                    eventDataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(EventListActivity.this, "Error getting events", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
