package com.oopsipushedtomain.Events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.DialogInputListeners.CustomDateTimePickerDialog;
import com.oopsipushedtomain.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     * The date formatter
     */
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    /**
     * Reference to the events database
     */
    private FirebaseAccess database = new FirebaseAccess(FirestoreAccessType.EVENTS);

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

        // Set the database


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
        // Start time button
        startTimeButton.setOnClickListener(v -> {
            // This should be a date picker
            CustomDateTimePickerDialog datePickerDialog = new CustomDateTimePickerDialog(this, input -> {
                // Convert the input to a date
                Date inputDate = (Date) input;

                // Format the given date, ChatGPT: How do i format a string into date
                String startDateString = formatter.format(inputDate);

                // Print the date to the screen
                newEventStartTimeEdit.setText(startDateString);

            });
            datePickerDialog.show("Edit Start Date/Time", new Date());

        });

        // End time button
        endTimeButton.setOnClickListener(v -> {
            // This should be a date picker
            CustomDateTimePickerDialog datePickerDialog = new CustomDateTimePickerDialog(this, input -> {
                // Convert the input to a date
                Date inputDate = (Date) input;

                // Format the given date, ChatGPT: How do i format a string into date
                String startDateString = formatter.format(inputDate);

                // Print the date to the screen
                newEventEndTimeEdit.setText(startDateString);


            });
            datePickerDialog.show("Edit Start Date/Time", new Date());

        });

        // Create button
        newEventCreateButton.setOnClickListener(v -> {
            // Check for valid event details
            if (!checkForValidEvent()) {
                // Do not create a new event yet
                Toast.makeText(NewEventActivity.this, "Some event details are missing", Toast.LENGTH_LONG).show();
                return;
            }

            // Date storage
            Date date = null;

            // Create a new event
            Event newEvent = new Event();

            // Title
            newEvent.setTitle(newEventTitleEdit.getText().toString());

            // Start time
            try {
                date = formatter.parse(newEventStartTimeEdit.getText().toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            newEvent.setStartTime(date);

            // End time
            date = null;
            try {
                date = formatter.parse(newEventEndTimeEdit.getText().toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            newEvent.setEndTime(date);


            // Set the description
            newEvent.setDescription(newEventDescriptionEdit.getText().toString());

            // Attendee limit
            if (newEventAttendeeLimitEdit.getText().toString().isEmpty() || newEventAttendeeLimitEdit.getText() == null){
                newEvent.setAttendeeLimit(Integer.MAX_VALUE);
            } else {
                newEvent.setAttendeeLimit(Integer.parseInt(newEventAttendeeLimitEdit.getText().toString()));
            }

            // User ID
            newEvent.setCreatorId(creatorId);


            // Update the event in the database
            newEvent.addEventToDatabase();

            // Show confirmation
            Toast.makeText(NewEventActivity.this, "Event has been created", Toast.LENGTH_SHORT).show();

            // Move to the event details page
            Intent intent = new Intent(NewEventActivity.this, EventDetailsActivity.class);
            intent.putExtra("userId", creatorId);
            intent.putExtra("selectedEventId", newEvent.getEventId());

            // Start the activity
            startActivity(intent);

            // Close this page
            finish();

        });


        // Retrieve the userId passed from EventListActivity
        Intent intent = getIntent();
        if (intent != null) {
            creatorId = intent.getStringExtra("userId");
        }

    }

    /**
     * Checks if the entered details are valid
     * @return
     */
    public Boolean checkForValidEvent() {
        // Make sure none of the fields are null
        boolean isReady = true;


        // Title
        if (newEventTitleEdit.getText() == null){
            isReady = false;
        };

        // Start time
        if (newEventStartTimeEdit.getText() == null){
            isReady = false;
        } else {
            try {
                formatter.parse(newEventStartTimeEdit.getText().toString());
            } catch (ParseException e) {
                isReady = false;
            }
        }

        // End time
        if (newEventEndTimeEdit.getText() == null){
            isReady = false;
        } else {
            try {
                formatter.parse(newEventEndTimeEdit.getText().toString());
            } catch (ParseException e) {
                isReady = false;
            }
        }

        // Set the description
       if (newEventDescriptionEdit.getText() == null){
           isReady = false;
       };


        // Return the correct result
        if (isReady){
            // Event is ready
            return true;
        } else {
            // Event is not ready
            return false;
        }
    }

}