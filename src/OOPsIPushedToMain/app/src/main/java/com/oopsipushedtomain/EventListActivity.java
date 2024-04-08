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
import androidx.core.app.OnNewIntentProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    private FirebaseAccess firebaseAccess;

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

        // Set the database access
        firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Find the create and sort buttons
        eventCreateButton = findViewById(R.id.create_event_button);
        eventSortButton = findViewById(R.id.sort_events_button);
        eventList = findViewById(R.id.EventListView);

        // Retrieve the userId passed from ProfileActivity
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
        }

        // Set up the click listeners
        setupListeners();

        // Create the data list
        eventDataList = new ArrayList<>();

        // Set the list adapter
        eventAdapter = new EventListAdapter(eventDataList, this);
        eventList.setAdapter(eventAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear the current event list
        eventDataList.clear();

        // Get the list of all events
        getAllEvents();
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
                        getAllEvents();
                    } else if (itemId == R.id.sort_by_signed_up) {
                        getAllSignedUpEvents();
                    } else if (itemId == R.id.sort_by_checked_in) {
                        getAllCheckedInEvents();
                    } else if (itemId == R.id.sort_by_user_event) {
                        getAllCreatedEvents();
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
                if (selectedEvent != null) {// && selectedEvent.getEventId() != null) {
                    String eventId = selectedEvent.getEventId();
                    Log.d("EventListActivity", "Clicked event ID: " + eventId);

                    Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                    intent.putExtra("selectedEventId", selectedEvent.getEventId());
                    intent.putExtra("userId", userId); // Assuming userId is the ID of the current user
                    startActivity(intent);
                } else {
                    Log.e("EventListActivity", "The event or event ID is null.");
                }
            }
        });
    }


    private void getAllEvents() {
        // Clear the event list
        eventDataList.clear();

        // Get all of the events in the database
        firebaseAccess.getAllDocuments().thenAccept(dataList -> {
            // Go through the list and create a new event for each one
            for (Map<String, Object> eventData : dataList) {

                // Create a new event
                Event newEvent = new Event();

                // Assign all the parameters to the event
                newEvent.setEventId(eventData.get("UID").toString());
                newEvent.setTitle(eventData.get("title").toString());
                try {
                    newEvent.setStartTime(((Timestamp) eventData.get("startTime")).toDate());
                } catch (Exception e) {
                    newEvent.setStartTime(new Date());
                }
                try {
                    newEvent.setEndTime(((Timestamp) eventData.get("endTime")).toDate());
                } catch (Exception e) {
                    newEvent.setEndTime(new Date());
                }

                newEvent.setDescription(eventData.get("description").toString());

//                newEvent.setLocation(eventData.get("location").toString());

                newEvent.setAttendeeLimit(Integer.parseInt(eventData.get("attendeeLimit").toString()));
                if (eventData.get("creatorId") != null){
                    newEvent.setCreatorId(eventData.get("creatorId").toString());

                    // Add the event to the list
                    eventDataList.add(newEvent);
                } else {
                    newEvent.setCreatorId(userId);
                }



            }

            // Data is completely loaded, notify
            runOnUiThread(() -> eventAdapter.notifyDataSetChanged());
        });
    }

    private void getAllSignedUpEvents() {
        // Clear the list
        eventDataList.clear();

        // Get all of the events a user has signed up for
        FirebaseAccess userDatabase = new FirebaseAccess(FirestoreAccessType.USERS);
        FirebaseAccess eventsDatabase = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Create a callable
        Callable<Void> getDataTask = () -> {
            // Get the list of checked in events
            ArrayList<Map<String, Object>> eventList =  userDatabase.getAllDocuments(userId, FirebaseInnerCollection.signedUpEvents).get();

            // Check if this is null
            if (eventList == null){
                runOnUiThread(() -> {
                    EventListActivity.this.eventAdapter.notifyDataSetChanged();
                });

                return null;
            }

            // Iterate through the events
            Event newEvent;
            for (Map<String, Object> data : eventList){

                // Create a new event
                newEvent = new Event();

                // Get the event details from the database
                Map<String, Object> eventData = eventsDatabase.getDataFromFirestore((String) data.get("UID")).get();

                // Set the params
                newEvent.setEventId((String) eventData.get("UID"));
                newEvent.setTitle((String) eventData.get("title"));

                // Add the event to the list
                EventListActivity.this.eventDataList.add(newEvent);

            }

            // All done, notify that dataset has changed
            runOnUiThread(() -> {
                EventListActivity.this.eventAdapter.notifyDataSetChanged();
            });

            return null;

        };

        // Run the task
        eventsDatabase.callableToCompletableFuture(getDataTask);

    }

    public void getAllCheckedInEvents() {
        // Clear the list
        eventDataList.clear();

        // Get all of the events a user has signed up for
        FirebaseAccess userDatabase = new FirebaseAccess(FirestoreAccessType.USERS);
        FirebaseAccess eventsDatabase = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Create a callable
        Callable<Void> getDataTask = () -> {
            // Get the list of checked in events
            ArrayList<Map<String, Object>> eventList =  userDatabase.getAllDocuments(userId, FirebaseInnerCollection.checkedInEvents).get();

            // Check if this is null
            if (eventList == null){
                runOnUiThread(() -> {
                    EventListActivity.this.eventAdapter.notifyDataSetChanged();
                });
                return null;
            }

            // Iterate through the events
            Event newEvent;
            for (Map<String, Object> data : eventList){
                // Create a new event
                newEvent = new Event();

                // Get the event details from the database
                Map<String, Object> eventData = eventsDatabase.getDataFromFirestore((String) data.get("UID")).get();

                // Set the params
                newEvent.setEventId((String) eventData.get("UID"));
                newEvent.setTitle((String) eventData.get("title"));

                // Add the event to the list
                EventListActivity.this.eventDataList.add(newEvent);

            }

            // All done, notify that dataset has changed
            runOnUiThread(() -> {
                EventListActivity.this.eventAdapter.notifyDataSetChanged();
            });

            return null;

        };

        // Run the task
        eventsDatabase.callableToCompletableFuture(getDataTask);

    }

    public void getAllCreatedEvents() {
        // Clear the event list
        eventDataList.clear();

        // Get all of the events in the database
        firebaseAccess.getAllDocuments().thenAccept(dataList -> {
            // Go through the list and create a new event for each one
            for (Map<String, Object> eventData : dataList) {

                // Check if the event is created by the current logged in user
                if (Objects.equals((String) eventData.get("creatorId"), userId)) {
                    // Create a new event
                    Event newEvent = new Event();

                    // Assign all the parameters to the event
                    newEvent.setEventId(eventData.get("UID").toString());
                    newEvent.setTitle(eventData.get("title").toString());
                    try {
                        newEvent.setStartTime(((Timestamp) eventData.get("startTime")).toDate());
                    } catch (Exception e) {
                        newEvent.setStartTime(new Date());
                    }
                    try {
                        newEvent.setEndTime(((Timestamp) eventData.get("endTime")).toDate());
                    } catch (Exception e) {
                        newEvent.setEndTime(new Date());
                    }

                    newEvent.setDescription(eventData.get("description").toString());

//                newEvent.setLocation(eventData.get("location").toString());

                    newEvent.setAttendeeLimit(Integer.parseInt(eventData.get("attendeeLimit").toString()));
                    newEvent.setCreatorId(eventData.get("creatorId").toString());

                    // Add the event to the list
                    eventDataList.add(newEvent);
                }
            }

            // Data is completely loaded, notify
            runOnUiThread(() -> eventAdapter.notifyDataSetChanged());
        });

    }

    private void fetchEvents() {
        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        });*/
        firebaseAccess.getAllDocuments().thenAccept(events -> {
            eventDataList.clear();


            eventDataList.addAll(events.stream().map(Event::new).collect(Collectors.toList()));
            runOnUiThread(() -> eventAdapter.notifyDataSetChanged());
        }).exceptionally(e -> {
            Log.e("EventListActivity", "Error fetching events", e);
            return null;
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
        db.collection("events").whereArrayContains("signedUpAttendees", userId).get().addOnCompleteListener(task -> {
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
        db.collection("events").whereEqualTo("creatorId", userId).get().addOnCompleteListener(task -> {
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
