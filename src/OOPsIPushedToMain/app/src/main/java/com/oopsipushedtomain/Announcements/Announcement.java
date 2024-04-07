package com.oopsipushedtomain.Announcements;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * This class represents Announcement objects in the app, with String fields for the announcements
 * title, body image UID, and event UID.
 *
 * @author Aidan Gironella
 * @see AnnouncementListActivity
 * @see AnnouncementListAdapter
 * @see SendAnnouncementActivity
 */
public class Announcement implements Serializable {
    /**
     * The title of the announcement
     */
    private String title;
    /**
     * The body of the announcement
     */
    private String body;

    /**
     * The image UID associated with this announcement
     */
    private String imageId;

    /**
     * The event UID associated with this announcement
     */
    private String eventId;

    private String anmtId;

    /**
     * Create an announcement
     *
     * @param title   The title of the announcement
     * @param body    The body of the announcement
     * @param imageId The image UID for the announcement
     * @param eventId The event UID for the announcement
     */
    public Announcement(String title, String body, String imageId, String eventId, String anmtId) {
        this.title = title;
        this.body = body;
        this.imageId = imageId;
        this.eventId = eventId;
        this.anmtId = anmtId;
    }

    /**
     * No-argument constructor so that Announcement can be deserialized
     */
    public Announcement() {
    }

    /**
     * Gets the announcement title
     *
     * @return Title of the announcement
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the announcement title
     *
     * @param title Title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the announcement body/main text
     *
     * @return Body/main text of the announcement
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the body/main text of the announcement
     *
     * @param body Body/main text to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets the image UID of the announcement
     *
     * @return Image UID of the announcement
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Sets the image UID of the announcement
     *
     * @param imageId Image UID to set
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * Gets the ID of the event that this announcement is for
     *
     * @return Unique event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the ID of the event that this announcement is for
     *
     * @param eventId Unique event ID to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the UID of this announcement
     * @return Unique announcement ID
     */
    public String getAnmtId() {
        return anmtId;
    }

    /**
     * Sets the UID of this announcement
     * @param anmtId Unique announcement ID to set
     */
    public void setAnmtId(String anmtId) {
        this.anmtId = anmtId;
    }

    /**
     * Prints an announcement details. Currently only used for debugging
     *
     * @return A string containing all of an announcement's details
     */
    @NonNull
    @Override
    public String toString() {
        return String.format("Title: %s\nBody: %s\neventId: %s\nimageId: %s\n", this.getTitle(), this.getBody(), this.getEventId(), this.getImageId());
    }
}
