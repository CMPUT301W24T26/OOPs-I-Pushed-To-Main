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
import java.util.Locale;

/**
 * EventDetailsActivityOrganizer allows organizers to view and edit details of an event.
 * It provides editable fields for the event's title, start time, end time, and description,
 * as well as an image view for the event's poster. Organizers can update event details,
 * which are intended to be saved to a persistent storage or backend upon confirmation.
 *
 * This activity also offers options to send notifications, view or limit attendees, view event QR code,
 * and delete the event. Each action is represented by a button, with placeholders for their functionalities.
 *
 * Outstanding issues:
 * 1. The implementation of dynamic image loading for the event poster is missing.
 * 2. The validation logic in validateInput() may need to be expanded based on additional requirements
 *    for the event details (e.g., date format validation).
 * 3. The saveEventDetails() method currently updates the EditText fields without performing any actual
 *    data persistence. Saving the updated event details to a database or backend service is required.
 * 4. The placeholder methods for sending notifications, viewing/limiting attendees, viewing event QR code,
 *    and deleting the event need to be fully implemented.
 * 5. The deletion logic in deleteEvent() is only a placeholder. Actual deletion from the data source
 *    needs to be implemented, along with notifying the EventListActivity to refresh its event list.
 */

public class EventDetailsActivityOrganizer extends AppCompatActivity {

    private EditText eventTitleEdit;
    private EditText eventStartTimeEdit;
    private EditText eventEndTimeEdit;
    private EditText eventDescriptionEdit;
    private ImageView eventPosterEdit;
    private Button eventSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        eventTitleEdit = findViewById(R.id.event_details_organizer_title_e);
        eventStartTimeEdit = findViewById(R.id.event_details_organizer_start_time_e);
        eventEndTimeEdit = findViewById(R.id.event_details_organizer_end_time_e);
        eventDescriptionEdit = findViewById(R.id.event_details_organizer_description_e);
        eventPosterEdit = findViewById(R.id.eventPosterImageViewEdit);
        eventSaveButton = findViewById(R.id.btnSaveEventDetails);

        eventStartTimeEdit.setOnClickListener(v -> showDateTimePicker(eventStartTimeEdit));
        eventEndTimeEdit.setOnClickListener(v -> showDateTimePicker(eventEndTimeEdit));

        Event event = (Event) getIntent().getSerializableExtra("selectedEvent");
        if (event != null) {
            // Set the text for the TextViews with event details
            eventTitleEdit.setText(event.getTitle());
            eventStartTimeEdit.setText(event.getStartTime());
            eventEndTimeEdit.setText(event.getEndTime());
            eventDescriptionEdit.setText(event.getDescription());
        }

        eventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start a new activity
                //Intent intent = new Intent(EventDetailsActivityOrganizer.this, ImageDetailsActivity.class);
                //startActivity(intent);
            }
        });

        // Set OnClickListener for the save button
        eventSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInput()){
                    saveEventDetails();
                    event.setTitle(eventTitleEdit.getText().toString());
                    event.setStartTime(eventStartTimeEdit.getText().toString());
                    event.setEndTime(eventEndTimeEdit.getText().toString());
                    event.setDescription(eventDescriptionEdit.getText().toString());
                    Intent intent = new Intent(EventDetailsActivityOrganizer.this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the back stack
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.btnSendNotification).setOnClickListener(v -> {
            //Intent intent = new Intent(this, SendNotificationActivity.class);
            //startActivity(intent);
        });

        findViewById(R.id.btnViewLimitAttendees).setOnClickListener(v -> {
            //Intent intent = new Intent(this, ViewLimitAttendeesActivity.class);
            //startActivity(intent);
        });

        findViewById(R.id.btnViewEventQRCode).setOnClickListener(v -> {
            //Intent intent = new Intent(this, ViewEventQRCodeActivity.class);
            //startActivity(intent);
        });

        findViewById(R.id.btnDeleteEvent).setOnClickListener(v -> {
            deleteEvent();
        });

    }

    private void saveEventDetails() {

        String title = eventTitleEdit.getText().toString();
        String startTime = eventStartTimeEdit.getText().toString();
        String endTime = eventEndTimeEdit.getText().toString();
        String description = eventDescriptionEdit.getText().toString();

        eventTitleEdit.setText(title);
        eventStartTimeEdit.setText(startTime);
        eventEndTimeEdit.setText(endTime);
        eventDescriptionEdit.setText(description);

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

    private boolean validateInput() {
        if (eventTitleEdit.getText().toString().trim().isEmpty() ||
                eventStartTimeEdit.getText().toString().trim().isEmpty() ||
                eventEndTimeEdit.getText().toString().trim().isEmpty() ||
                eventDescriptionEdit.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Additional validation logic can go here
        return true;
    }

    private void deleteEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = getIntent().getStringExtra("eventId");
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventDetailsActivityOrganizer.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to EventListActivity
                    Intent intent = new Intent(EventDetailsActivityOrganizer.this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the back stack
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(EventDetailsActivityOrganizer.this, "Error deleting event", Toast.LENGTH_SHORT).show());
    }



}