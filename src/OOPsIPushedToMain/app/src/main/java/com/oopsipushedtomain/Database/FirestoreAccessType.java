package com.oopsipushedtomain.Database;

/**
 * An enum for the main collections in Firestore
 */
public enum FirestoreAccessType {

    /**
     * The events collection
     * Contains information about events
     */
    EVENTS,
    /**
     * The images collection
     * Contains links between the image and the linked document
     */
    IMAGES,
    /**
     * The QR codes collection
     * Contains links between the QR code images and their data
     * Also includes the data of the QR code
     */
    QRCODES,
    /**
     * The users collection.
     * Contains information about the app's users
     */
    USERS

}
