package com.example.oopsipushedtomain.Announcements;

public class Announcement {
    private String title;
    private String body;
    private String imageId;
    private String eventId;

    Announcement(String title, String body, String imageId, String eventId) {
        this.title = title;
        this.body = body;
        this.imageId = imageId;
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
