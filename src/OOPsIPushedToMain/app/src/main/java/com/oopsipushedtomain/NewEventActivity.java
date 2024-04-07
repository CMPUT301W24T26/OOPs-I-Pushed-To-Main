package com.oopsipushedtomain;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator;

/**
 * NewEventActivity facilitates the creation of new events by organizers. It provides a form
 * for inputting event details such as the event title, start time, end time, and description.
 * Additionally, it offers an ImageView placeholder for future functionality to add an event poster.
 * <p>
 * Upon filling the form and clicking the 'create' button, the new event is intended to be added
 * to the event list, and the organizer is navigated back to the EventListActivity where the new event
 * will be displayed.
 * <p>
 * Outstanding issues:
 * 1. The event poster functionality is not yet implemented.
 */

public class NewEventActivity extends AppCompatActivity {

    /**
     * The edit texts for the title, description and attendee limit
     */
    private EditText newEventTitleEdit, newEventDescriptionEdit, newEventAttendeeLimitEdit;

    /**
     * The Text view for the start and end times
     */
    private TextView newEventStartTimeEdit, newEventEndTimeEdit;

    /**
     * The buttons for editing the start and end time
     */
    private Button startTimeButton, endTimeButton;


    /**
     * The UID of the creator
     */
    private String creatorId;

    /**
     * The reference to the create event button
     */
    private Button newEventCreateButton;

    /**
     * Initializes all parameters of this class including the click listeners and database
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // The event details
        newEventTitleEdit = findViewById(R.id.new_event_title_e);
        newEventStartTimeEdit = findViewById(R.id.new_event_start_time_e);
        newEventEndTimeEdit = findViewById(R.id.new_event_end_time_e);
        newEventDescriptionEdit = findViewById(R.id.new_event_description_e);
        newEventAttendeeLimitEdit = findViewById(R.id.new_event_attendee_limit_e);

        // The time edit buttons
        startTimeButton = findViewById(R.id.edit_event_start_button);
        endTimeButton = findViewById(R.id.edit_event_end_button);

        // The create new event button
        newEventCreateButton = findViewById(R.id.btnCreateNewEvent);

        /*
            Click listeners
         */





        setupListeners();
        // Retrieve the userId passed from EventListActivity
        Intent intent = getIntent();
        if (intent != null) {
            creatorId = intent.getStringExtra("userId");
        }

    }


    /**
     * Sets all of the on click listeners for the class
     */
    private void setupListeners() {
        newEventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start a new activity
                //Intent intent = new Intent(EventDetailsActivityOrganizer.this, ImageDetailsActivity.class);
                //startActivity(intent);
            }
        });

        // TODO: Fix event creation to support times
        newEventCreateButton.setOnClickListener(v -> {
            // Capture input from user
            String title = newEventTitleEdit.getText().toString();
            String startTime = newEventStartTimeEdit.getText().toString();
            String endTime = newEventEndTimeEdit.getText().toString();
            String description = newEventDescriptionEdit.getText().toString();
            String attendeeLimitStr = newEventAttendeeLimitEdit.getText().toString();
            int attendeeLimit = attendeeLimitStr.isEmpty() ? 0 : Integer.parseInt(attendeeLimitStr);
            // TODO: Add functionality for location, posterURL
            Event newEvent = new Event(title, startTime, null, null, "testlocation", "testURL", attendeeLimit, creatorId);
            newEvent.addEventToDatabase();
            finish();
            //Intent intent = new Intent(NewEventActivity.this, EventDetailsActivity.class);
            //startActivity(intent);
        });
    }

    /**
     * Shows a date time picker
     *
     * @param editText The edit text you are editing
     */
    private void showDateTimePicker(final EditText editText) {
        Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar time = Calendar.getInstance();
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                time.set(Calendar.YEAR, year);
                time.set(Calendar.MONTH, month);
                time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                time.set(Calendar.MINUTE, minute);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                editText.setText(dateFormat.format(time.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }
}