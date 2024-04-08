package com.oopsipushedtomain;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.oopsipushedtomain.Announcements.SendAnnouncementActivity;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;
import com.oopsipushedtomain.DialogInputListeners.CustomDatePickerDialog;
import com.oopsipushedtomain.DialogInputListeners.CustomDateTimePickerDialog;
import com.oopsipushedtomain.DialogInputListeners.InputTextDialog;
import com.oopsipushedtomain.Geolocation.MapActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Time;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    /**
     * Event poster image
     */
    Bitmap eventPoster = null;
    /**
     * UID for event poster
     */
    String eventPosterUID = null;
    /**
     * Text views for event details
     */
    private TextView eventTitle, eventStartTime, eventEndTime, eventDescription;
    /**
     * Buttons for editing the event details
     */
    private Button eventTitleButton, eventStartTimeButton, eventEndTimeButton, eventDescriptionButton;
    /**
     * The view for the event image poster
     */
    private ImageView eventPosterEdit;
    /**
     * The references to the buttons
     */
    private Button eventSaveButton, sendNotificationButton, viewAnnouncementsButton, signUpButton, deleteButton, viewEventQRCodeButton, viewMapButton, viewEventPromoQRCodeButton, viewSignedUpButton, viewCheckedInButton, getViewSignedUpButton;
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
    private FirebaseAccess database;
    /**
     * The result launcher for getting the photo from the gallery
     */
    private final ActivityResultLauncher<String> galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            // Handle the selected image URI
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(result);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Get the bitmap image
            InputStream finalInputStream = inputStream;
            Bitmap picture = BitmapFactory.decodeStream(finalInputStream);

            if (result != null) {
                // Set in view
                eventPosterEdit.setImageURI(result);
                // Set event image in the database
                database.storeImageInFirestore(event.getEventId(), null, ImageType.eventPosters, picture);
            }
        }
    });
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private User user;
    private boolean userIsOrganizer = false;
    /**
     * Boolean to check if the event poster is the default image
     */
    private boolean isDefaultPoster = true;

    /**
     * Initializes the class with all parameters
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default for creating a new view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Create a new future for getting the event and user
        CompletableFuture<Void> eventFuture = new CompletableFuture<>();
        CompletableFuture<Void> userFuture = new CompletableFuture<>();

        // Get the database reference
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Get the required details from passed from the calling activity
        // User ID
        Intent intent2 = getIntent();
        userId = intent2.getStringExtra("userId");
        eventID = intent2.getStringExtra("selectedEventId");

        // Create a new empty event
        event = new Event();

        // Using the event ID, get the data from Firestore
        database.getDataFromFirestore(eventID).thenAccept(eventData -> {
            // If the event does not exists, throw an error
            if (eventData == null) {
                Log.e("EventDetails", "The event does not exist!");
            }
            // Assign all the parameters to the event
            event.setEventId(eventData.get("UID").toString());
            event.setTitle(eventData.get("title").toString());
            event.setStartTime(((Timestamp) eventData.get("startTime")).toDate());
            event.setEndTime(((Timestamp) eventData.get("endTime")).toDate());
            event.setDescription(eventData.get("description").toString());
            event.setAttendeeLimit(Integer.parseInt(eventData.get("attendeeLimit").toString()));
            event.setCreatorId(eventData.get("creatorId").toString());

            // Set the details on the screen
            runOnUiThread(() -> {
                eventTitle.setText(eventData.get("title").toString());
                if (eventData.get("startTime") != null) {
                    eventStartTime.setText(formatter.format(((Timestamp) eventData.get("startTime")).toDate()));
                }
                if (eventData.get("endTime") != null) {
                    eventEndTime.setText(formatter.format(((Timestamp) eventData.get("endTime")).toDate()));
                }
                eventDescription.setText(eventData.get("description").toString());
            });

            database.getAllRelatedImagesFromFirestore(event.getEventId(), ImageType.eventPosters).thenAccept(dataList -> {
                if (dataList == null) {
                    // If the datalist is empty, there is no profile picture
                    this.eventPoster = null;
                    // Set default image to true
                    isDefaultPoster = true;
                } else {
                    // Set the UID and profile image if there's an existing image
                    this.eventPoster = (Bitmap) dataList.get(0).get("image");
                    this.eventPosterUID = (String) dataList.get(0).get("UID");
                    // Custom poster exists
                    isDefaultPoster = false;
                    // Set the image
                    runOnUiThread(() -> {
                        eventPosterEdit.setImageBitmap(this.eventPoster);
                    });
                }
            });


            // Finished getting the event details
            eventFuture.complete(null);
        });

        // Create a new user from the UserID
        User.createNewObject(userId).thenAccept(newUser -> {
            user = newUser;

            // Finished getting the user
            userFuture.complete(null);
        });


        // Find the texts for the event details
        eventTitle = findViewById(R.id.event_details_organizer_title_e);
        eventStartTime = findViewById(R.id.event_details_organizer_start_time_e);
        eventEndTime = findViewById(R.id.event_details_organizer_end_time_e);
        eventDescription = findViewById(R.id.event_details_organizer_description_e);

        // Event poster
        eventPosterEdit = findViewById(R.id.eventPosterImageViewEdit);

        // Buttons
        eventSaveButton = findViewById(R.id.btnSaveEventDetails);
        sendNotificationButton = findViewById(R.id.btnSendNotification);
        viewAnnouncementsButton = findViewById(R.id.btnViewAnnouncements);
        signUpButton = findViewById(R.id.btnSignUpEvent);
        deleteButton = findViewById(R.id.btnDeleteEvent);
        viewEventQRCodeButton = findViewById(R.id.btnViewEventQRCode);
        viewEventPromoQRCodeButton = findViewById(R.id.btnViewPromoQRCode);
        viewMapButton = findViewById(R.id.btnViewMap);
        viewSignedUpButton = findViewById(R.id.btnViewSignedUp);
        viewCheckedInButton = findViewById(R.id.btnViewCheckedIn);


        // Edit event buttons
        eventTitleButton = findViewById(R.id.edit_event_title_button);
        eventStartTimeButton = findViewById(R.id.edit_event_start_button);
        eventEndTimeButton = findViewById(R.id.edit_event_end_button);
        eventDescriptionButton = findViewById(R.id.edit_event_description_button);

        // If we have both the event and the user, check to see if the user is the organizer of this event
        userFuture.thenAccept(userVal -> {
            eventFuture.thenAccept(eventVal -> {
                // Compare event organizer and the current user
                if (Objects.equals(user.getUID(), event.getCreatorId())) {
                    // They are the organizer, show relevant buttons
                    userIsOrganizer = true;

                    runOnUiThread(() -> {
                        // Event details
                        eventTitleButton.setVisibility(View.VISIBLE);
                        eventStartTimeButton.setVisibility(View.VISIBLE);
                        eventEndTimeButton.setVisibility(View.VISIBLE);
                        eventDescriptionButton.setVisibility(View.VISIBLE);

                        // Delete button
                        deleteButton.setVisibility(View.VISIBLE);

                        // Event options
                        sendNotificationButton.setVisibility(View.VISIBLE);
                        viewCheckedInButton.setVisibility(View.VISIBLE);
                        viewSignedUpButton.setVisibility(View.VISIBLE);
                        viewEventQRCodeButton.setVisibility(View.VISIBLE);
                        viewMapButton.setVisibility(View.VISIBLE);
                    });
                }


            });
        });

        /*
            Click Listeners
         */

        // Start Date
        eventStartTimeButton.setOnClickListener(v -> {
            // This should be a date picker
            CustomDateTimePickerDialog datePickerDialog = new CustomDateTimePickerDialog(this, input -> {
                // Convert the input to a date
                Date inputDate = (Date) input;

                // Format the given date, ChatGPT: How do i format a string into date
                String startDateString = formatter.format(inputDate);

                // Print the date to the screen
                eventStartTime.setText(startDateString);

                // Show the save button
                eventSaveButton.setVisibility(View.VISIBLE);

            });
            datePickerDialog.show("Edit Start Date/Time", new Date());


        });

        // End Date
        eventEndTimeButton.setOnClickListener(v -> {
            // This should be a date picker
            CustomDateTimePickerDialog datePickerDialog = new CustomDateTimePickerDialog(this, input -> {
                // Convert the input to a date
                Date inputDate = (Date) input;

                // Format the given date, ChatGPT: How do i format a string into date
                String startDateString = formatter.format(inputDate);

                // Print the date to the screen
                eventEndTime.setText(startDateString);

                // Show the save button
                eventSaveButton.setVisibility(View.VISIBLE);

            });
            datePickerDialog.show("Edit Start Date/Time", new Date());


        });

        // Event Title
        eventTitleButton.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    eventTitle.setText((String) input);
                }

                // Show the save button
                eventSaveButton.setVisibility(View.VISIBLE);
            });

            // Show the dialog with the old value
            textDialog.show("Edit Nickname", eventTitle.getText().toString());

        });

        // Event Description
        eventDescriptionButton.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    eventDescription.setText((String) input);
                }

                // Show the save button
                eventSaveButton.setVisibility(View.VISIBLE);
            });

            // Show the dialog with the old value
            textDialog.show("Edit Nickname", eventDescription.getText().toString());
        });

        // Save button
        eventSaveButton.setOnClickListener(v -> {
            // Create a map to update the event in the database
            Map<String, Object> eventMap = new HashMap<>();

            // Set the event title
            if (eventTitle.getText() != null) {
                event.setTitle(eventTitle.getText().toString());
                eventMap.put("title", eventTitle.getText().toString());
            }


            // Set the start time
            Date date = null;
            if (eventStartTime.getText() != null) {
                try {
                    date = formatter.parse(eventStartTime.getText().toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                event.setStartTime(date);
                eventMap.put("startTime", date);
            }

            // Set the end time
            date = null;
            if (eventEndTime.getText() != null) {
                try {
                    date = formatter.parse(eventEndTime.getText().toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                event.setEndTime(date);
                eventMap.put("endTime", date);
            }

            // Set the description
            if (eventDescription.getText() != null) {
                event.setDescription(eventDescription.getText().toString());
                eventMap.put("description", eventDescription.getText().toString());
            }

            // Update the event in the database
            database.storeDataInFirestore(eventID, eventMap);

            // Hide the save button
            eventSaveButton.setVisibility(View.GONE);

            // Show confirmation
            Toast.makeText(EventDetailsActivity.this, "Event Details have been saved", Toast.LENGTH_SHORT).show();

        });

        // Custom event poster upload/deletion handling
        eventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User can only change the event poster if they are an Organizer
                if (!userIsOrganizer) {
                    return;
                }
                // Build a new alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                builder.setTitle("Update Event Poster");
                if (isDefaultPoster) {
                    // If the current image is the default image, don't show the delete option
                    builder.setItems(new CharSequence[]{"Choose from Gallery"},
                            (dialog, which) -> {
                                if (which == 0) {
                                    galleryResultLauncher.launch("image/*");
                                    // Event poster is no longer default image
                                    isDefaultPoster = false;
                                }
                            });
                } else {
                    // If the image isn't the default image, give option to delete
                    builder.setItems(new CharSequence[]{"Choose from Gallery", "Delete Poster"},
                            (dialog, which) -> {
                                switch (which) {
                                    case 0: // Choose from Gallery
                                        galleryResultLauncher.launch("image/*");
                                        Log.d("Gallery", "Poster image selected from the gallery");
                                        // Event poster is not default
                                        isDefaultPoster = false;
                                        break;

                                    case 1: // Delete Poster
                                        // If poster is deleted, set back to default poster image
                                        eventPosterEdit.setImageResource(R.drawable.default_event_poser);
                                        // Update image to null in the database
                                        database.deleteImageFromFirestore(event.getEventId(), eventPosterUID, ImageType.eventPosters);
                                        // Event poster is set to default
                                        isDefaultPoster = true;
                                        break;
                                }
                            });
                }
                builder.show();
            }
        });

        // Sign up button
        signUpButton.setOnClickListener(v -> {
            // Check if the event has room to sign up
            database.getDataFromFirestore(eventID).thenAccept(data -> {
                int signedUpUsers;
                int attendeeLimit = Integer.MAX_VALUE;

                // Check the number of signed up users
                if (data.get("signedUp") != null) {
                    signedUpUsers =  data.get("signedUp") instanceof Number ? ((Number) data.get("signedUp")).intValue() : 0;
                } else {
                    signedUpUsers = 0;
                }

                // Check the number of signed up users
                if (data.get("attendeeLimit") != null) {
                    attendeeLimit = data.get("attendeeLimit") instanceof Number ? ((Number) data.get("attendeeLimit")).intValue() : 0;
                }

                // Check if the user can be signed up
                if (signedUpUsers < attendeeLimit) {
                    // The user can be signed up
                    user.signUp(eventID).thenAccept(isNew -> {
                        // If it is a new sign up, increment the counter
                        if (isNew) {
                            Map<String, Object> newData = new HashMap<>();
                            newData.put("signedUp", signedUpUsers + 1);

                            // Store in database
                            runOnUiThread(() -> {
                                database.storeDataInFirestore(eventID, newData);
                            });

                            // Sign the user up for notifications
                            FirebaseMessaging.getInstance().subscribeToTopic(eventID);

                            // Notify the user
                            runOnUiThread(() -> Toast.makeText(EventDetailsActivity.this, "Signed up to attend this event!", Toast.LENGTH_SHORT).show());

                        }
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(EventDetailsActivity.this, "Cannot sign up for event: Attendee limit reached.", Toast.LENGTH_SHORT).show());
                }


            });
        });


//
//            // Sign the user up for notifications
//            FirebaseMessaging.getInstance().subscribeToTopic(event.getEventId());
//
//            // Sign in the user to the event
//            user.signUp(eventID);
//
//            // Fetch the event details to check the attendee limit and current signed-up attendees
//            FirebaseAccess eventAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);
//            eventAccess.getDataFromFirestore(eventID).thenAccept(eventData -> {
//                if (eventData != null) {
//                    int attendeeLimit = eventData.containsKey("attendeeLimit") ? ((Number) eventData.get("attendeeLimit")).intValue() : Integer.MAX_VALUE;
//                    List<String> signedUpAttendees = eventData.containsKey("signedUpAttendees") ? (List<String>) eventData.get("signedUpAttendees") : new ArrayList<>();
//
//                    // Check if the user is already signed up
//                    if (signedUpAttendees.contains(userId)) {
//                        // User is already signed up, show confirmation
//                        runOnUiThread(() -> Toast.makeText(EventDetailsActivity.this, "You are already signed up for this event!", Toast.LENGTH_SHORT).show());
//                    } else {
//                        // Check if the attendee limit has been reached
//                        if (signedUpAttendees.size() < attendeeLimit) {
//                            // The limit has not been reached, proceed to sign up the user
//                            signedUpAttendees.add(userId);
//
//                            // Update the event with the new list of signed-up attendees
//                            Map<String, Object> update = new HashMap<>();
//                            update.put("signedUpAttendees", signedUpAttendees);
//                            eventAccess.storeDataInFirestore(eventID, update);
//
//                            // Show confirmation
//                            runOnUiThread(() -> Toast.makeText(EventDetailsActivity.this, "You have successfully signed up for the event!", Toast.LENGTH_SHORT).show());
//                        } else {
//                            // The attendee limit has been reached
//                            runOnUiThread(() -> Toast.makeText(EventDetailsActivity.this, "Cannot sign up for event: Attendee limit reached.", Toast.LENGTH_SHORT).show());
//                        }
//                    }
//                } else {
//                    Log.e("EventDetailsActivity", "Event not found: " + eventID);
//                }
//            }).exceptionally(e -> {
//                Log.e("EventDetailsActivity", "Error signing up for event: " + eventID, e);
//                return null;
//            });


        // Send notification button
        sendNotificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, SendAnnouncementActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        // View announcements list button
        viewAnnouncementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, AnnouncementListActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        // View and limit attendees button
        viewSignedUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, ViewLimitAttendeesActivity.class);
            intent.putExtra("eventId", event.getEventId()); // Pass the event ID to the new activity
            startActivity(intent);
        });

        // View event QR code button
        viewEventQRCodeButton.setOnClickListener(v -> {
            ImageType imageType = ImageType.eventQRCodes;

            QRCode.loadQRCodeObject(eventID, imageType).thenAccept(qrCode -> {
                Bitmap qrCodeBitmap = qrCode.getQRCodeImage();

                if (qrCodeBitmap != null) {
                    runOnUiThread(() -> {
                        // Save the bitmap to a file and get its URI
                        Uri qrCodeUri = saveBitmapToFile(qrCodeBitmap, "qrCodeImage.png");

                        if (qrCodeUri != null) {
                            // Start ShowImageActivity with the URI of the saved image
                            Intent intent = new Intent(EventDetailsActivity.this, ShowImageActivity.class);
                            intent.putExtra("qrCodeUri", qrCodeUri.toString()); // Pass URI as a string
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EventDetailsActivity.this, "Failed to load QR Code image.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(EventDetailsActivity.this, "QR Code image not available.", Toast.LENGTH_SHORT).show());
                }
            }).exceptionally(exception -> {
                Log.e("QRCodeLoading", "Error loading QR Code", exception);
                return null;
            });
        });

        // View promo qr code button
        viewEventPromoQRCodeButton.setOnClickListener(v -> {
            ImageType imageType = ImageType.promoQRCodes;

            QRCode.loadQRCodeObject(eventID, imageType).thenAccept(qrCode -> {
                Bitmap qrCodeBitmap = qrCode.getQRCodeImage();

                if (qrCodeBitmap != null) {
                    runOnUiThread(() -> {
                        // Save the bitmap to a file and get its URI
                        Uri qrCodeUri = saveBitmapToFile(qrCodeBitmap, "qrCodeImage.png");

                        if (qrCodeUri != null) {
                            // Start ShowImageActivity with the URI of the saved image
                            Intent intent = new Intent(EventDetailsActivity.this, ShowImageActivity.class);
                            intent.putExtra("qrCodeUri", qrCodeUri.toString()); // Pass URI as a string
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EventDetailsActivity.this, "Failed to load QR Code image.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(EventDetailsActivity.this, "QR Code image not available.", Toast.LENGTH_SHORT).show());
                }
            }).exceptionally(exception -> {
                Log.e("QRCodeLoading", "Error loading QR Code", exception);
                return null;
            });
        });

        // View map button
        viewMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("eventId", eventID);
            startActivity(intent);
        });

        // Delete event button
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

    private Uri saveBitmapToFile(Bitmap bitmap, String fileName) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs(); // Create directories if needed
            FileOutputStream stream = new FileOutputStream(cachePath + "/" + fileName); // Overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File imagePath = new File(getCacheDir(), "images");
            File newFile = new File(imagePath, fileName);
            return FileProvider.getUriForFile(this, "com.oopsipushedtomain.fileprovider", newFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes an event from firestore
     *
     * @param eventId The id of the event to delete
     */
    private void deleteEvent(String eventId) { // eventId passed as a parameter
        // Deletes the event
        database.deleteDataFromFirestore(eventId);
    }

    /**
     * Returns the current event ID. Used for Intent testing (MapActivity)
     *
     * @return The event ID
     */
    public String getEventID() {
        return eventID;
    }


}