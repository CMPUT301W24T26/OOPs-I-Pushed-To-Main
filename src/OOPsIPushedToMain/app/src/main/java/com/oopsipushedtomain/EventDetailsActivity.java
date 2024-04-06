package com.oopsipushedtomain;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.zxing.qrcode.encoder.QRCode;
import com.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.oopsipushedtomain.Announcements.SendAnnouncementActivity;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;
import com.oopsipushedtomain.Geolocation.MapActivity;
import com.oopsipushedtomain.QRCode;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * EventDetailsActivity allows organizers to view and edit details of an event.
 * It provides editable fields for the event's title, start time, end time, and description,
 * as well as an image view for the event's poster. Organizers can update event details,
 * which are intended to be saved to a persistent storage or backend upon confirmation.
 * <p>
 * This activity also offers options to send notifications, view or limit attendees, view event QR code,
 * and delete the event. Each action is represented by a button, with placeholders for their functionalities.
 * <p>
 * Outstanding issues:
 * 1. The implementation of dynamic image loading for the event poster is missing.
 * 2. The validation logic in validateInput() may need to be expanded based on additional requirements
 * for the event details (e.g., date format validation).
 * 3. The saveEventDetails() method currently updates the EditText fields without performing any actual
 * data persistence. Saving the updated event details to a database or backend service is required.
 * 4. The placeholder methods for sending notifications, viewing/limiting attendees, viewing event QR code,
 * and deleting the event need to be fully implemented.
 */
public class EventDetailsActivity extends AppCompatActivity {
    // TODO: Change logic to hide buttons depending on user type
    /**
     * The view of the event title
     */
    private EditText eventTitleEdit;
    /**
     * The view of the event start time
     */
    private EditText eventStartTimeEdit;
    /**
     * The view of the event end time
     */
    private EditText eventEndTimeEdit;
    /**
     * The view of the event description
     */
    private EditText eventDescriptionEdit;
    /**
     * The view for the event image poster
     */
    private ImageView eventPosterEdit;
    /**
     * The references to the buttons
     */
    private Button eventSaveButton, sendNotificationButton, viewAnnouncementsButton, signUpButton, viewLimitAttendeeButton, deleteButton, viewEventQRCodeButton, viewMapButton, viewEventPromoQRCodeButton;

    /**
     * The UID of the user
     */
    private String userId;

    /**
     * The UID of the event
     */
    private String eventID;

    /**
     * Event
     */
    private Event event;

    /**
     * Initializes the class with all parameters
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        CompletableFuture<Event> future = new CompletableFuture<>();

        eventTitleEdit = findViewById(R.id.event_details_organizer_title_e);
        eventStartTimeEdit = findViewById(R.id.event_details_organizer_start_time_e);
        eventEndTimeEdit = findViewById(R.id.event_details_organizer_end_time_e);
        eventDescriptionEdit = findViewById(R.id.event_details_organizer_description_e);
        eventPosterEdit = findViewById(R.id.eventPosterImageViewEdit);
        eventSaveButton = findViewById(R.id.btnSaveEventDetails);
        sendNotificationButton = findViewById(R.id.btnSendNotification);
        viewAnnouncementsButton = findViewById(R.id.btnViewAnnouncements);
        signUpButton = findViewById(R.id.btnSignUpEvent);
        viewLimitAttendeeButton = findViewById(R.id.btnViewLimitAttendees);
        deleteButton = findViewById(R.id.btnDeleteEvent);
        viewEventQRCodeButton = findViewById(R.id.btnViewEventQRCode);
        viewEventPromoQRCodeButton = findViewById(R.id.btnViewPromoQRCode);
//        currentUserUID = CustomFirebaseAuth.getInstance().getCurrentUserID();
        viewMapButton = findViewById(R.id.btnViewMap);

        eventStartTimeEdit.setOnClickListener(v -> showDateTimePicker(eventStartTimeEdit));
        eventEndTimeEdit.setOnClickListener(v -> showDateTimePicker(eventEndTimeEdit));

        // Retrieve the userId passed from EventListActivity
        Intent intent_a = getIntent();
        if (intent_a != null) {
            userId = intent_a.getStringExtra("userId");
        }

        eventID = getIntent().getStringExtra("selectedEventId");
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);
        event = new Event();
        try {
            firebaseAccess.getDataFromFirestore(eventID).thenAccept(datalist -> {
                if (datalist == null) {
                    Log.e("EventDetailsActivity", "event is null");
                } else {
//                    datalist.forEach((key, value) -> Log.d("map", key + ":" + value));
//                    event = new Event(
//                            datalist.get("UID").toString(),
//                            datalist.get("title").toString(),
//                            datalist.get("startTime").toString(),
//                            datalist.get("description").toString(),
//                            datalist.get("location").toString(),
//                            datalist.get("posterUrl").toString(),
//                            (int) datalist.get("attendeeLimit"),
//                            datalist.get("creatorId").toString()
//                    );
//                    event = new Event();
                    event.setEventId(datalist.get("UID").toString());
                    event.setTitle(datalist.get("title").toString());
                    event.setStartTime(datalist.get("startTime").toString());
                    event.setDescription(datalist.get("description").toString());
                    event.setLocation(datalist.get("location").toString());
                    event.setPosterUrl(datalist.get("posterUrl").toString());
                    event.setAttendeeLimit(Integer.parseInt(datalist.get("attendeeLimit").toString()));
                    event.setCreatorId(datalist.get("creatorId").toString());

                    // Set the text for the TextViews with event details
                    Log.d("EventDetailsActivity", event.getTitle());
                    runOnUiThread(() -> {
                        eventTitleEdit.setText(event.getTitle());
                        eventStartTimeEdit.setText(event.getStartTime());
                        eventEndTimeEdit.setText(event.getEndTime());
                        eventDescriptionEdit.setText(event.getDescription());

                        // TODO: not currently working
//                        determineUserRole(userId, eventID, this::updateUIForRole);
                    });
                }
            });
        } catch (Exception e) {
            Log.e("EventDetailsActivity", String.valueOf(e));
        }

        eventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start a new activity
                //Intent intent = new Intent(EventDetailsActivity.this, ImageDetailsActivity.class);
                //startActivity(intent);
            }
        });

        // Set OnClickListener for the save button
        eventSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    saveEventDetails();
                    event.setTitle(eventTitleEdit.getText().toString());
                    event.setStartTime(eventStartTimeEdit.getText().toString());
                    event.setEndTime(eventEndTimeEdit.getText().toString());
                    event.setDescription(eventDescriptionEdit.getText().toString());
                    Intent intent = new Intent(EventDetailsActivity.this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the back stack
                    startActivity(intent);
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpForEvent(event.getEventId());
                FirebaseMessaging.getInstance().subscribeToTopic(event.getEventId());
            }
        });

        sendNotificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, SendAnnouncementActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        viewAnnouncementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, AnnouncementListActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        viewLimitAttendeeButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, ViewLimitAttendeesActivity.class);
            intent.putExtra("eventId", event.getEventId()); // Pass the event ID to the new activity
            startActivity(intent);
        });

        viewEventQRCodeButton.setOnClickListener(v -> {
            ImageType imageType = ImageType.eventQRCodes;

            // Call the asynchronous method to get the QRCode object
            QRCode.loadQRCodeObject(eventID, imageType).thenAccept(qrCode -> {
                // This code block is executed asynchronously when the QRCode object is available
                Bitmap qrCodeBitmap = qrCode.getQRCodeImage();

                // Check if the QR code bitmap is not null
                if (qrCodeBitmap != null) {
                    // Since this is an asynchronous callback, ensure you run UI operations on the UI thread
                    runOnUiThread(() -> {
                        // Create and show the ShowImageFragment with the QRCode image
                        ShowImageFragment showImageFragment = ShowImageFragment.newInstance(qrCodeBitmap);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.image_view, showImageFragment) // Make sure this is the ID of your actual container
                                .addToBackStack(null) // Optional: Add this transaction to the back stack
                                .commit();
                    });
                } else {
                    // Handle the case where the QR code bitmap is null, also on the UI thread
                    runOnUiThread(() ->
                            Toast.makeText(EventDetailsActivity.this, "QR Code image not available.", Toast.LENGTH_SHORT).show());
                }
            }).exceptionally(exception -> {
                // Handle any exceptions here
                Log.e("QRCodeLoading", "Error loading QR Code", exception);
                return null;
            });
        });

        viewEventPromoQRCodeButton.setOnClickListener(v -> {
            ImageType imageType = ImageType.promoQRCodes;

            // Call the asynchronous method to get the QRCode object
            QRCode.loadQRCodeObject(eventID, imageType).thenAccept(qrCode -> {
                // This code block is executed asynchronously when the QRCode object is available
                Bitmap qrCodeBitmap = qrCode.getQRCodeImage();

                // Check if the QR code bitmap is not null
                if (qrCodeBitmap != null) {
                    // Since this is an asynchronous callback, ensure you run UI operations on the UI thread
                    runOnUiThread(() -> {
                        // Create and show the ShowImageFragment with the QRCode image
                        ShowImageFragment showImageFragment = ShowImageFragment.newInstance(qrCodeBitmap);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.image_view, showImageFragment) // Make sure this is the ID of your actual container
                                .addToBackStack(null) // Optional: Add this transaction to the back stack
                                .commit();
                    });
                } else {
                    // Handle the case where the QR code bitmap is null, also on the UI thread
                    runOnUiThread(() ->
                            Toast.makeText(EventDetailsActivity.this, "QR Code image not available.", Toast.LENGTH_SHORT).show());
                }
            }).exceptionally(exception -> {
                // Handle any exceptions here
                Log.e("QRCodeLoading", "Error loading QR Code", exception);
                return null;
            });
        });



        viewMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("eventId", eventID);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            final String eventId = getIntent().getStringExtra("selectedEventId");
            // Call deleteEvent with the eventId
            if (eventId != null) {
                deleteEvent(eventId);
            } else {
                Toast.makeText(this, "Event ID is not available.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Saves the event details to the class parameters
     */
    private void saveEventDetails() {

        String title = eventTitleEdit.getText().toString();
        String startTime = eventStartTimeEdit.getText().toString();
        String endTime = eventEndTimeEdit.getText().toString();
        String description = eventDescriptionEdit.getText().toString();

        eventTitleEdit.setText(title);
        eventStartTimeEdit.setText(startTime);
        eventEndTimeEdit.setText(endTime);
        eventDescriptionEdit.setText(description);

        Map<String, Object> updatedEventData = new HashMap<>();
        updatedEventData.put("title", title);
        updatedEventData.put("startTime", startTime);
        updatedEventData.put("endTime", endTime);
        updatedEventData.put("description", description);
        // Add any other event details you want to update

        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);
        firebaseAccess.storeDataInFirestore(eventID, updatedEventData);

    }

    /**
     * Shows a date and time picker
     *
     * @param editText The edit text being edited
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

    /**
     * Validates the event parameters
     *
     * @return Whether the check passes
     */
    private boolean validateInput() {
        if (eventTitleEdit.getText().toString().trim().isEmpty() || eventStartTimeEdit.getText().toString().trim().isEmpty() || eventEndTimeEdit.getText().toString().trim().isEmpty() || eventDescriptionEdit.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Additional validation logic can go here
        return true;
    }

    /**
     * Deletes an event from firestore
     *
     * @param eventId The id of the event to delete
     */
    private void deleteEvent(String eventId) { // eventId passed as a parameter
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);
        firebaseAccess.deleteDataFromFirestore(eventId);
        finish();
        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
            // Intent to navigate back or simply finish this activity
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show());*/
    }

    /**
     * Signs a user up for an event
     * @param eventId The id of the event
     */
    private void signUpForEvent(String eventId) {
        // test geolocation
        FirebaseAccess eventDB = new FirebaseAccess(FirestoreAccessType.EVENTS);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("Geolocation", "User has location disabled");
        }
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                            Log.d("Geolocation", String.valueOf(point));
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("coordinates", point);
                            data.put("userId", userId);
                            data.put("timestamp", new Timestamp(System.currentTimeMillis()));
                            eventDB.storeDataInFirestore(eventID, FirebaseInnerCollection.checkInCoords, null, data);
                        }
                    }
                });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Event event = document.toObject(Event.class);
                    if (event != null && event.getSignedUpAttendees().size() < event.getAttendeeLimit()) {
                        // Proceed with signing up the user
                        eventRef.update("signedUpAttendees", FieldValue.arrayUnion(userId))
                                .addOnSuccessListener(aVoid -> Toast.makeText(EventDetailsActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Sign up failed, limit exceeded", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(EventDetailsActivity.this, "Event is full", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    /**
     * An enum for the user roles
     * TODO: Need to change to proper parameters
     */
    public enum UserRole {
        ORGANIZER, ATTENDEE_NOT_SIGNED_UP, ATTENDEE_SIGNED_UP
    }

    /**
     * Determines the role of the user for the given event
     * @param userId The UID of the user
     * @param eventId The UID of the event
     * @param callback The listener to deal with the result
     *
     *  TODO: Move this to the User class
     */
    public void determineUserRole(String userId, String eventId, final UserRoleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference createdRef = db.collection("users").document(userId).collection("created").document(eventId);
        DocumentReference signedUpRef = db.collection("users").document(userId).collection("signedup").document(eventId);

        createdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                callback.onRoleDetermined(UserRole.ORGANIZER);
            } else {
                // Not an organizer, check if attendee
                signedUpRef.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().exists()) {
                        callback.onRoleDetermined(UserRole.ATTENDEE_SIGNED_UP);
                    } else {
                        callback.onRoleDetermined(UserRole.ATTENDEE_NOT_SIGNED_UP);
                    }
                });
            }
        });
    }

    /**
     * The interface for determining when data is received
     */
    public interface UserRoleCallback {
        /**
         * Callback for returning the role of the user once it has been found
         * @param role The role of the user
         */
        void onRoleDetermined(UserRole role);
    }

    /**
     * Hides specific buttons depending on the role of the user
     * @param role The role of the user (user/admin)
     */
    private void updateUIForRole(UserRole role) {
        switch (role) {
            case ORGANIZER:
                // Organizer should see all buttons except sign up button
                eventSaveButton.setVisibility(View.VISIBLE);
                sendNotificationButton.setVisibility(View.VISIBLE);
                viewAnnouncementsButton.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
                viewLimitAttendeeButton.setVisibility(View.VISIBLE);
                viewEventQRCodeButton.setVisibility(View.VISIBLE);
                break;
            case ATTENDEE_SIGNED_UP:
                // Signed-up attendee should not see any button
                eventSaveButton.setVisibility(View.GONE);
                sendNotificationButton.setVisibility(View.GONE);
                viewAnnouncementsButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                viewLimitAttendeeButton.setVisibility(View.GONE);
                viewEventQRCodeButton.setVisibility(View.GONE);
                break;
            case ATTENDEE_NOT_SIGNED_UP:
                // Unsigned-up attendee should see the sign up button
                eventSaveButton.setVisibility(View.GONE);
                sendNotificationButton.setVisibility(View.GONE);
                viewAnnouncementsButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                viewLimitAttendeeButton.setVisibility(View.GONE);
                viewEventQRCodeButton.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Returns the current event ID. Used for Intent testing (MapActivity)
     * @return The event ID
     */
    public String getEventID() {
        return eventID;
    }


}