package com.example.oopsipushedtomain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
 * 3. The event list is not dynamically fetched from a database or backend service. The application needs to
 *    implement data persistence and retrieval mechanisms to fetch events dynamically and manage them accordingly.
 */

public class EventListActivity extends AppCompatActivity {

    private ListView eventList;
    private ArrayList<Event> eventDataList;
    private ArrayAdapter<Event> eventAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        eventDataList = new ArrayList<>();
        eventList = findViewById(R.id.EventListView);
        eventAdapter = new EventListAdapter(eventDataList, this);
        eventList.setAdapter(eventAdapter);

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) parent.getItemAtPosition(position);
                Intent intent;

                // TODO: Replace the following condition with actual logic to determine user role
                if (true) {
                    intent = new Intent(EventListActivity.this, EventDetailsActivityAttendee.class);
                } else {
                    intent = new Intent(EventListActivity.this, EventDetailsActivityOrganizer.class);
                }

                intent.putExtra("selectedEvent", selectedEvent);
                startActivity(intent);
            }
        });
    }
}
