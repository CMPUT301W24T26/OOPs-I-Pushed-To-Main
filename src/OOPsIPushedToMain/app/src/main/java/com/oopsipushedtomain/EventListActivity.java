package com.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import java.util.ArrayList;

/**
 * EventListActivity is responsible for displaying a list of events to the user.
 * It acts as the entry point for users to view events, and upon selection, provides detailed information
 * about the selected event. This activity supports different actions based on the user's role (attendee or organizer).
 * <p>
 * Outstanding issues:
 * 1. The user role check is currently hardcoded to always true, which does not reflect the actual user role.
 * This needs to be replaced with a dynamic check to determine if the user is an attendee or an organizer.
 * 2. The deletion logic in EventDetailsActivityOrganizer does not currently update the event list in this activity.
 * A mechanism to refresh the event list after an event is deleted needs to be implemented.
 */

public class EventListActivity extends AppCompatActivity {

    /**
     * The view to show the list of events
     */
    private ListView eventList;
    /**
     * The array list to store the events
     */
    private ArrayList<Event> eventDataList;
    /**
     * The adapter to show an event
     */
    private ArrayAdapter<Event> eventAdapter;

    /**
     * The UID of the user
     */
    private String userId;

    private Button eventCreateButton, eventSortButton;

    /**
     * Initializes the parameters of the class
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        eventCreateButton = findViewById(R.id.create_event_button);
        eventSortButton = findViewById(R.id.sort_events_button);

        // Retrieve the userId passed from ProfileActivity
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
        }

        initializeViews();

        setupListeners();
    }

    /**
     * Initializes the views in the layout
     */
    private void initializeViews() {
        eventDataList = new ArrayList<>();
        eventList = findViewById(R.id.EventListView);
        eventAdapter = new EventListAdapter(eventDataList, this);
        eventList.setAdapter(eventAdapter);
    }

    /**
     * Sets up click listeners for the buttons on this page
     */
    private void setupListeners() {
        // Create Event button functionality
        eventCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to the event creation activity
                Intent intent = new Intent(EventListActivity.this, NewEventActivity.class);
                intent.putExtra("userId", userId); // Assuming userId is the ID of the current user
                startActivity(intent);
            }
        });

        // Sort Events button functionality
        eventSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EventListActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.event_sort_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.sort_by_all_events) {
                        fetchEvents();
                    } else if (itemId == R.id.sort_by_signed_up) {
                        fetchSignedUpEvents(userId);
                    } else if (itemId == R.id.sort_by_user_event) {
                        fetchUserCreatedEvents(userId);
                    } else if (itemId == R.id.sort_by_date) {
                        fetchEventsSortedByDate();
                    }
                    return true;
                });
                popup.show();
            }
        });

        // Click on an event in the list functionality
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Assuming the adapter is populated with Event objects, you can cast directly to Event
                Event selectedEvent = (Event) parent.getItemAtPosition(position);
                if (selectedEvent != null && selectedEvent.getEventId() != null) {
                    String eventId = selectedEvent.getEventId();
                    Log.d("EventListActivity", "Clicked event ID: " + eventId);

                    Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                    intent.putExtra("selectedEvent", selectedEvent);
                    intent.putExtra("userId", userId); // Assuming userId is the ID of the current user
                    startActivity(intent);
                } else {
                    Log.e("EventListActivity", "The event or event ID is null.");
                }
            }
        });
    }

    /**
     * Refresh from the database when this activity is shown on the screen
     */
    @Override
    protected void onResume() {
        super.onResume();


        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);
        firebaseAccess.getAllEvents().thenAccept(events -> {
            eventDataList.clear();
            eventDataList.addAll(events);
            runOnUiThread(() -> eventAdapter.notifyDataSetChanged());
        }).exceptionally(e -> {
            // Handle exceptions
            Log.e("EventListActivity", "Error fetching events", e);
            return null;
        });


        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventDataList.clear();
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        // Set the eventId of the Event object to the document ID
                        event.setEventId(documentSnapshot.getId());
                        eventDataList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(EventListActivity.this, "Error getting events", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void fetchEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventDataList.clear();
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    Event event = documentSnapshot.toObject(Event.class);
                    eventDataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            } else {
                Log.e("EventListActivity", "Error getting events", task.getException());
            }
        });
    }

    private void fetchEventsSortedByDate() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").orderBy("startTime", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventDataList.clear();
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    Event event = documentSnapshot.toObject(Event.class);
                    eventDataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            } else {
                Log.e("EventListActivity", "Error getting sorted events", task.getException());
            }
        });

    }

    private void fetchSignedUpEvents(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").whereArrayContains("signedUpAttendees", userId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventDataList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Event event = documentSnapshot.toObject(Event.class);
                            eventDataList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("EventListActivity", "Error getting signed up events", task.getException());
                    }
                });
    }

    private void fetchUserCreatedEvents(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").whereEqualTo("creatorId", userId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventDataList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Event event = documentSnapshot.toObject(Event.class);
                            eventDataList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("EventListActivity", "Error getting user created events", task.getException());
                    }
                });
    }
}
