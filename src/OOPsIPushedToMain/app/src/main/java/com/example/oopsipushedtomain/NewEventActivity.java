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
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
            // Assuming handling location, posterUrl, qrCodeData, and attendeeLimit elsewhere

            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("startDateTime", startTime);
            event.put("endDateTime", endTime);
            event.put("details", description);
            // Add other fields as necessary

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(NewEventActivity.this, "Event added successfully", Toast.LENGTH_SHORT).show();
                        // Navigate back to EventListActivity
                        Intent intent = new Intent(NewEventActivity.this, EventListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(NewEventActivity.this, "Error adding event", Toast.LENGTH_SHORT).show());
        });
    }
    private void showDateTimePicker (final EditText editText){
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