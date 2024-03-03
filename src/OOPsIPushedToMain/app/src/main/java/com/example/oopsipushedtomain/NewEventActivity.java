package com.example.oopsipushedtomain;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * NewEventActivity facilitates the creation of new events by organizers. It provides a form
 * for inputting event details such as the event title, start time, end time, and description.
 * Additionally, it offers an ImageView placeholder for future functionality to add an event poster.
 *
 * Upon filling the form and clicking the 'create' button, the new event is intended to be added
 * to the event list, and the organizer is navigated back to the EventListActivity where the new event
 * will be displayed.
 *
 * Outstanding issues:
 * 1. The event poster functionality is not yet implemented.
 * 2. Actual addition to the data source needs to be implemented, along with notifying the EventListActivity to refresh its event list.
 */

public class NewEventActivity extends AppCompatActivity {

    private EditText newEventTitleEdit;
    private EditText newEventStartTimeEdit;
    private EditText newEventEndTimeEdit;
    private EditText newEventDescriptionEdit;
    private ImageView newEventPosterEdit;
    private Button newEventCreateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        newEventTitleEdit = findViewById(R.id.new_event_title_e);
        newEventStartTimeEdit = findViewById(R.id.new_event_start_time_e);
        newEventEndTimeEdit = findViewById(R.id.new_event_end_time_e);
        newEventDescriptionEdit = findViewById(R.id.new_event_description_e);
        newEventPosterEdit = findViewById(R.id.newEventPosterImageViewEdit);
        newEventCreateButton = findViewById(R.id.btnCreateNewEvent);

        newEventStartTimeEdit.setOnClickListener(v -> showDateTimePicker(newEventStartTimeEdit));
        newEventEndTimeEdit.setOnClickListener(v -> showDateTimePicker(newEventEndTimeEdit));

        newEventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start a new activity
                //Intent intent = new Intent(EventDetailsActivityOrganizer.this, ImageDetailsActivity.class);
                //startActivity(intent);
            }
        });

        newEventCreateButton.setOnClickListener(v -> {
            // Capture input from user
            String title = newEventTitleEdit.getText().toString();
            String startTime = newEventStartTimeEdit.getText().toString();
            String endTime = newEventEndTimeEdit.getText().toString();
            String description = newEventDescriptionEdit.getText().toString();
            // Assuming you handle location, posterUrl, qrCodeData, and attendeeLimit elsewhere

            // Create the event object
            Event newEvent = new Event(UUID.randomUUID().toString(), title, startTime, endTime, description, "", "", "", 0);

            // TODO: Add the new event to the list

            // Navigate back to EventListActivity
            Intent intent = new Intent(NewEventActivity.this, EventListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

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