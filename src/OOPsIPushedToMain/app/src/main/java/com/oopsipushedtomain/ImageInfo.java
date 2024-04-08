package com.oopsipushedtomain;

import android.graphics.Bitmap;

/**
 * A class representing the information about an image, including the image itself,
 * its storage path on Firebase Storage, and optionally its Firestore document ID.
 */
public class ImageInfo {
    // The Bitmap representation of the image.
    /**
     * The Bitmap of the image
     */
    private Bitmap image;
    // The storage path of the image in Firebase Storage.
    /**
     * The path to the image in Firebase Storage
     */
    private String storagePath;
    /**
     * The image UID associated with the image
     */
    private String firestoreDocumentId;

    /**
     * The origin document
     */
    private String origin;

    /**
     * Constructs a new ImageInfo instance.
     *
     * @param image               The Bitmap representation of the image.
     * @param firestoreDocumentId The Firestore document ID associated with the image. This parameter is optional and can be null.
     * @param origin The origin document
     */
    public ImageInfo(Bitmap image, String firestoreDocumentId, String origin) {
        this.image = image;
        this.firestoreDocumentId = firestoreDocumentId;
        this.origin = origin;
    }

    /**
     * Returns the image as a Bitmap.
     *
     * @return The Bitmap representation of the image.
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * Sets the image from a Bitmap.
     *
     * @param image The Bitmap representation of the new image.
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }

    /**
     * Returns the storage path of the image in Firebase Storage.
     *
     * @return The storage path as a String.
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Sets the storage path of the image in Firebase Storage.
     *
     * @param storagePath The new storage path as a String.
     */
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Returns the Firestore document ID associated with the image, if any.
     *
     * @return The Firestore document ID as a String, or null if not set.
     */
    public String getFirestoreDocumentId() {
        return firestoreDocumentId;
    }

    /**
     * Sets the Firestore document ID associated with the image.
     *
     * @param firestoreDocumentId The Firestore document ID as a String. This can be null.
     */
    public void setFirestoreDocumentId(String firestoreDocumentId) {
        this.firestoreDocumentId = firestoreDocumentId;
    }

    /**
     * Sets the origin associated with the image.
     *
     * @return The origin path as a String.
     */
    public String getOrigin() {
        return origin;
    }
}
