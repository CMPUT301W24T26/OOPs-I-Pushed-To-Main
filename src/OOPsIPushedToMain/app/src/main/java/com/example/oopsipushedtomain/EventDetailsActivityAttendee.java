package com.example.oopsipushedtomain;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * EventDetailsActivityAttendee displays detailed information about an event to attendees.
 * This activity is intended for attendees to view more information about a selected event,
 * including its title, start and end times, description, and an image representing the event.
 *
 * It retrieves the Event object passed through an Intent from the EventListActivity and
 * populates the UI elements with the event's details.
 *
 * Outstanding issues:
 * 1. The activity currently does not handle the loading of event poster images dynamically.
 *    Implementing functionality to load images into the ImageView is needed.
 * 2. The activity does not provide any interactive features for attendees, such as the option to
 *    sign up for the event, share the event details, or navigate to the event location.
 */

public class EventDetailsActivityAttendee extends AppCompatActivity {

    private TextView eventTitle;
    private TextView eventStartTime;
    private TextView eventEndTime;
    private TextView eventDescription;
    private ImageView eventPoster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_attendee);

        eventTitle = findViewById(R.id.event_details_attendee_title_a);
        eventStartTime = findViewById(R.id.event_details_attendee_start_time_a);
        eventEndTime = findViewById(R.id.event_details_attendee_end_time_a);
        eventDescription = findViewById(R.id.event_details_attendee_description_a);
        eventPoster = findViewById(R.id.eventPosterImageView);
        // Retrieve the Event object
        Event event = (Event) getIntent().getSerializableExtra("selectedEvent");

        if (event != null) {
            // Set the text for the TextViews with event details
            eventTitle.setText(event.getTitle());
            eventStartTime.setText(event.getStartTime());
            eventEndTime.setText(event.getEndTime());
            eventDescription.setText(event.getDescription());
        }

    }


}