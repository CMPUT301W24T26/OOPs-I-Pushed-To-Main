package com.oopsipushedtomain.Events;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;
import com.oopsipushedtomain.QRCode.QRCode;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event within the application.
 * This class is used to model events, including their details such as title, start and end times,
 * description, location, poster URL, QR code data, and attendee limit. It implements Serializable
 * to allow event instances to be passed between activities or components.
 * <p>
 * Outstanding issues: None known at this time.
 */
public class Event implements Serializable {
    /**
     * The UID of the event
     */
    private String eventId = null;
    /**
     * The title of the event
     */
    private String title;
    /**
     * The start time of the event
     */
    private Date startTime;
    /**
     * The end time of the event
     */
    private Date endTime;
    /**
     * The event description
     */
    private String description;
    /**
     * The event location
     */
    private String location; // Optional

    /**
     * The UID for the event poster
     */
    private String posterUrl;
    /**
     * The maximum number of attendees that can check into the event
     */
    private int attendeeLimit; // Optional

    /**
     * The UID of user who created the event
     */
    private String creatorId; // New attribute for the creator's ID

    /**
     * The UID of the event poster image
     */
    private String imageUID = null;


    // Database parameters
    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db;
    /**
     * A reference to the events collection
     */
    private CollectionReference eventRef;


    // Firebase storage
    /**
     * A reference to Firebase Storage where images are stored
     */
    private FirebaseStorage storage;
    /**
     * A reference to the storage pool for images
     */
    private StorageReference storageRef;

    private FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);

    Timestamp tempTimestamp = null;

    private int signedUpUsers = 0;

    /**
     * Count that keeps track of how many unique check-ins there have been
     */
    private int checkIns = 0;


    /**
     * Constructs a new Event instance.
     *
     * @param title         The title of the event.
     * @param startTime     The start time of the event.
     * @param endTime       The end time of the event.
     * @param description   A description of the event.
     * @param location      The location of the event.
     * @param attendeeLimit The maximum number of attendees for the event. Use 0 or a negative number to indicate no limit.
     */
    public Event(String title, Date startTime, Date endTime, String description, String location,  int attendeeLimit, String creatorId) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location; // Optional
        this.attendeeLimit = attendeeLimit; // Optional
        this.creatorId = creatorId;
    }

    /**

     * Constructs a new Event instance with a given eventID.
     *
     * @param eventId       The id of the event
     * @param title         The title of the event.
     * @param startTime     The start time of the event.
     * @param endTime       The end time of the event.
     * @param description   A description of the event.
     * @param location      The location of the event.
     * @param attendeeLimit The maximum number of attendees for the event. Use 0 or a negative number to indicate no limit.
     */
    public Event(String eventId, String title, Date startTime, Date endTime, String description, String location, int attendeeLimit, String creatorId) {
        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location; // Optional
        this.attendeeLimit = attendeeLimit; // Optional
        this.creatorId = creatorId;
    }

    /**
     * Constructs an Event instance from a map of properties.
     * This constructor is useful for creating an Event object from Firestore document data.
     *
     * @param properties A map containing event properties.
     */
    public Event(Map<String, Object> properties) {
        this.eventId = (String) properties.get("eventId");
        this.title = (String) properties.get("title");


        if (properties.get("startTime") != null) {
            this.startTime = ((Timestamp) properties.get("startTime")).toDate();
        } else {
            this.startTime = new Date();
        }

        if (properties.get("endTime") != null) {
            this.endTime = ((Timestamp) properties.get("endTime")).toDate();
        } else {
            this.endTime = new Date();
        }


        this.description = (String) properties.get("description");
        this.location = (String) properties.get("location");
        this.attendeeLimit = properties.get("attendeeLimit") instanceof Number ? ((Number) properties.get("attendeeLimit")).intValue() : 0;
        this.creatorId = (String) properties.get("creatorId");
        if (properties.get("signedUp") != null) {
            this.signedUpUsers = properties.get("signedUp") instanceof Number ? ((Number) properties.get("attendeeLimit")).intValue() : 0;
        }
        this.checkIns = properties.get("checkIns") instanceof Number ? ((Number) properties.get("checkIns")).intValue() : 0;
    }



    /**
     * No-argument constructor so that Event can be deserialized
     */
    public Event() {
    }

    /**
     * Listener for determining when a bitmap file is received from the database
     */
    public interface OnBitmapReceivedListener {
        /**
         * Returns the bitmap to the calling function
         * @param bitmap The bitmap to return
         */
        void onBitmapReceived(Bitmap bitmap);
    }


    /**
     * Adds the current object to the database
     */
    public void addEventToDatabase() {
        // Create the map to add the event to the database
        Map<String, Object> eventData = prepareEventData();

        // Create the event in the database
        try {
            // Store in database
            Map<String, Object> result = firebaseAccess.storeDataInFirestore(null, eventData);
            Log.d("Event", "Event added/updated successfully: " + result);

            // Get the created eventID
            eventId = (String) result.get("outer");

            // Generate the two QR codes
            QRCode.createNewQRCodeObject(eventId, ImageType.promoQRCodes);
            QRCode.createNewQRCodeObject(eventId, ImageType.eventQRCodes);

            // Sign organizer up for milestone notifications
            FirebaseMessaging.getInstance().subscribeToTopic(eventId + "-ORGANIZER");

        } catch (Exception e) {
            Log.e("Event", "Error adding/updating event", e);
        }

    }

    private Map<String, Object> prepareEventData() {
        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("startTime", startTime);
        event.put("endTime", endTime);
        event.put("description", description);
        event.put("location", location);
        event.put("attendeeLimit", attendeeLimit);
        event.put("creatorId", creatorId);
        event.put("signedUp", signedUpUsers);
        event.put("checkIns", checkIns);
        return event;
    }

    /**
     * Gets the unique identifier for the event.
     *
     * @return the event's unique identifier
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the unique identifier for the event.
     *
     * @param eventId the unique identifier to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the title of the event.
     *
     * @return the event's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the start time of the event.
     *
     * @return the event's start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the event.
     *
     * @param startTime the start time to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time of the event.
     *
     * @return the event's end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the event.
     *
     * @param endTime the end time to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the description of the event.
     *
     * @return the event's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the location of the event. This field is optional.
     *
     * @return the event's location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event. This field is optional.
     *
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the URL of the event's poster.
     *
     * @return the URL of the event's poster
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Sets the URL of the event's poster.
     *
     * @param posterUrl the URL to set
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Gets the limit of attendees for the event. This field is optional.
     *
     * @return the attendee limit
     */
    public int getAttendeeLimit() {
        return attendeeLimit;
    }

    /**
     * Sets the limit of attendees for the event. This field is optional.
     *
     * @param attendeeLimit the attendee limit to set
     */
    public void setAttendeeLimit(int attendeeLimit) {
        this.attendeeLimit = attendeeLimit;
    }

    /**
     * Gets the creatorId for the event.
     * @return the creatorId
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the creatorId of the event.
     *
     * @param creatorId the creatorId to set
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getSignedUpUsers() {
        return signedUpUsers;
    }

    public void setSignedUpUsers(int signedUpUsers) {
        this.signedUpUsers = signedUpUsers;
    }

    public int getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(int checkIns) {
        this.checkIns = checkIns;
    }
}
