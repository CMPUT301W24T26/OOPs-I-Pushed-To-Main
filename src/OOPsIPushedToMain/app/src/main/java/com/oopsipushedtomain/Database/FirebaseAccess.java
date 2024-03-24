package com.oopsipushedtomain.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oopsipushedtomain.Event;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class for all Firebase accesses
 * Has methods for accessing both storage and Firestore database
 *
 * @author Matteo Falsetti
 * @version 1.0
 * @see FirebaseInnerCollection
 * @see FirestoreAccessType
 * @see ImageType
 */
public class FirebaseAccess {

    /**
     * A reference to the collection
     */
    private CollectionReference collRef = null;

    /**
     * A reference to the document
     */
    private DocumentReference docRef = null;

    /**
     * The type of the database access (Ex. Events)
     */
    private FirestoreAccessType databaseType;

    /**
     * Creates a new FirebaseAccess object
     *
     * @param databaseType The data you want to access in the database
     */
    public FirebaseAccess(FirestoreAccessType databaseType) {
        // Set the database and storage type
        this.databaseType = databaseType;

        // Set the database reference (same for the entire database)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Set the collection
        switch (databaseType) {
            case ANNOUNCEMENTS:
                collRef = db.collection("announcements");
                break;
            case EVENTS:
                collRef = db.collection("events");
                break;
            case IMAGES:
                collRef = db.collection("images");
                break;
            case QRCODES:
                collRef = db.collection("qrcodes");
                break;
            case USERS:
                collRef = db.collection("users");
                break;
        }
    }

    // Chat GPT: Is there a way to wait until data is confirmed stored in Firebase database using a future

    /**
     * Attach a CompletableFuture to a Firebase read/write
     *
     * @param task The task to attach the future to
     * @param <T>  The parameter for the Completable future
     * @return The CompletableFuture to it was attached to
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Task<T> task) {
        // Create the new completable future
        CompletableFuture<T> future = new CompletableFuture<>();

        // Add the listeners for the the task
        task.addOnSuccessListener(future::complete).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    // ChatGPT: What is the best way to convert a bitmap into a string
    // ChatGPT: Is there a more compressed encoding? Can Firestore store a byte array?

    /**
     * Converts a bitmap image to a Blob
     *
     * @param bitmap The bitmap to convert
     * @return The output Blob
     */
    public static Blob bitmapToBlob(Bitmap bitmap) {
        // Try to compress to PNG
        boolean imageCompressed = false;
        byte[] bitmapBytes = null;

        // Create a new output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Compress
        Log.d("Image Compression", "Compressing to PNG");
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmapBytes = baos.toByteArray();

        // Check if the required size is reached
//        Log.d("Image Compression", "Size: " + bitmapBytes.length);
        if (bitmapBytes.length <= 1048487) {
            imageCompressed = true;
        }


        // Try to compress to JPEG if image is not small enough
        int quality = 100;
        while (!imageCompressed) {
            // Create a new output
            baos = new ByteArrayOutputStream();

            Log.d("Image Compression", "Compressing JPEG at quality " + quality);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            bitmapBytes = baos.toByteArray();

            // Check if the required size is reached
//            Log.d("Image Compression", "Size: " + bitmapBytes.length);
            if (bitmapBytes.length <= 1048487) {
                imageCompressed = true;
            } else {
                if (quality == 0) {
                    break;
                }
                quality = quality / 2;
            }

        }


        // Convert the byte array to a Blob
        return Blob.fromBytes(bitmapBytes);
    }

    /**
     * Converts a Blob to a bitmap image
     *
     * @param imageBlob The Blob to convert
     * @return The bitmap image
     */
    public static Bitmap blobToBitmap(Blob imageBlob) {
        // Convert the string to a byte array
        byte[] byteArray = imageBlob.toBytes();

        // Convert the byte array back to a bitmap
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }


    // Chat GPT: Is there a way to wait until data is confirmed stored in Firebase database using a future

    /**
     * Stores data in Firestore, given the UID of the document.
     * If it is a new document, it will create a UID
     * Assumes the document is an outer document
     *
     * @param docName The UID of the document to write to
     * @param data    The data to write to the document
     * @return A map containing the UID of the outer document Map.get("outer")
     */
    public Map<String, String> storeDataInFirestore(String docName, Map<String, Object> data) {
        return this.storeDataInFirestore(docName, null, null, data);
    }

    /**
     * Stores data in Firestore, given the UID of the document and inner collection/document if needed
     * If the inner document or collection are null, the data is stored in the outer document
     * If the outer doc name is not given, make a new one
     *
     * @param outerDocName  The UID of the outer document (Ex. Event UID)
     * @param innerCollName The name of the inner collection, if null, store in outer document
     * @param innerDocName  The UID of the inner document (Ex. eventPosters), if null, store in outer document
     * @param data          The data to write to the inner document
     * @return A map containing the UID of the outer document Map.get("outer") and the UID of the inner document Map.get("inner")
     */
    public Map<String, String> storeDataInFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName, Map<String, Object> data) {
        // If the outerDocName is not given, make a new one
        if (outerDocName == null) {
            switch (databaseType) {
                case EVENTS:
                    outerDocName = "EVNT-" + collRef.document().getId().toUpperCase();
                    break;
                case USERS:
                    outerDocName = "USER-" + collRef.document().getId().toUpperCase();
                    break;
                case ANNOUNCEMENTS:
                    outerDocName = "ANMT-" + collRef.document().getId().toUpperCase();
                    break;
                case QRCODES:
                    Log.e("StoreinFirestore", "Use storeImageInFirebaseStorage() to store qrcodes");
                    return null;
                case IMAGES:
                    Log.e("StoreinFirestore", "Use storeImageInFirebaseStorage() to store an image");
                    return null;
            }
        }

        // If the inner document name is not specified, create a new one
        // Essentially a special case for announcements
        if (innerDocName == null && innerCollName != null) {
            switch (innerCollName) {
                case eventPosters:
                case eventQRCodes:
                case promoQRCodes:
                case profilePictures:
                    throw new IllegalArgumentException("Use storeImageInFirestore instead for " + innerCollName.name());
                case announcements:
                    innerDocName = "ANMT-" + collRef.document().getId().toUpperCase();
                    break;
                case checkedInEvents:
                case signedUpEvents:
                    throw new IllegalArgumentException("Create an event first!!");
            }
        } else if (innerCollName == null && innerDocName != null) {
            throw new IllegalArgumentException("Inner collection must be specified!");
        }

        // Set the document reference
        docRef = collRef.document(outerDocName);

        // Store the data
        Task<Void> task = null;
        if (innerCollName != null && innerDocName != null) {
            task = docRef.collection(innerCollName.name()).document(innerDocName).set(data);
        } else {
            task = docRef.set(data);
        }

        // Create a completable future for waiting until the transfer is complete
        CompletableFuture<Void> future = toCompletableFuture(task);

        // Catch any errors
        try {
            // Block until data is written
            future.get();
            Log.d("StoreinFirestore", "Document has been saved successfully");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("StoreinFirestore", "Task was interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirestore", "Error writing document: " + Objects.requireNonNull(e.getCause()).getMessage());
        }

        // Return the UID of the outer document
        Map<String, String> outNames = new HashMap<>();
        outNames.put("outer", outerDocName);
        outNames.put("inner", innerDocName);
        return outNames;
    }

    /**
     * Saves an image to firestore and creates a link to it
     *
     * @param attachedTo The UID/name of the document this image is linked to. Ex: USER-0000000000
     * @param imageUID   The UID/name of the image (will create new if null)
     * @param imageType  The type of image you are uploading (only used for events)
     * @param image      The image to upload
     * @return The UID of the image or null if there was an error
     */
    public String storeImageInFirestore(String attachedTo, String imageUID, ImageType imageType, Bitmap image) {
        return storeImageInFirestore(attachedTo, imageUID, imageType, image, null);
    }

    /**
     * Saves an image to firestore and creates a link to it
     * Also allows for attaching extra data to the image
     *
     * @param attachedTo The UID/name of the document this image is linked to. Ex: USER-0000000000
     * @param imageUID   The UID/name of the image (will create new if null)
     * @param imageType  The type of image you are uploading (only used for events)
     * @param image      The image to upload
     * @param imageData  Any other data to add to the image
     * @return The UID of the image or null if there was an error
     */
    public String storeImageInFirestore(String attachedTo, String imageUID, ImageType imageType, Bitmap image, Map<String, Object> imageData) {
        // Check if this the constructor database parameters are correct
        boolean isValidDatabase = this.databaseType == FirestoreAccessType.USERS || this.databaseType == FirestoreAccessType.EVENTS;

        // Throw exception if the database is not valid
        if (!isValidDatabase) {
            throw new IllegalArgumentException("This object is not valid for storing images");
        }

        // Check if extra data exists
        if (imageData == null) {
            imageData = new HashMap<>();
        }

        // Database is valid, set the document reference for the origin document
        docRef = collRef.document(attachedTo);

        // If no image UID is given, create a new one
        boolean newImage = false;
        if (imageUID == null) {
            if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
                imageUID = "IMGE-" + collRef.document().getId().toUpperCase();
            } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
                imageUID = "QRCD-" + collRef.document().getId().toUpperCase();
            }
        }

        // Convert the image to a Blob
        Blob imageBlob = bitmapToBlob(image);

        if (imageBlob.toBytes().length > 1048487) {
            // Image is too large
            throw new IllegalArgumentException("Image is too large: " + imageBlob.toBytes().length + " bytes.");
        }

        // Empty map for forcing document creation in origin
        HashMap<String, Object> originData = new HashMap<>();

        // Map for the data in the appropriate images collection
        imageData.put("image", imageBlob);
        imageData.put("origin", attachedTo);
        imageData.put("type", imageType.name());

        // The access to the correct image database
        FirebaseAccess imageAccess = null;


        // Get the correct database to store the image and store the origin in the correct inner collection
        switch (this.databaseType) {
            case EVENTS:
                switch (imageType) {
                    case eventPosters:
                        imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventPosters, imageUID, originData);
                        break;
                    case eventQRCodes:
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventQRCodes, imageUID, originData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                        break;
                    case promoQRCodes:
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.promoQRCodes, imageUID, originData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                        break;
                }
                break;
            case USERS:
                this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.profilePictures, imageUID, originData);
                imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                break;
        }


        // Store the image in the database
        assert imageAccess != null;
        imageAccess.storeDataInFirestore(imageUID, imageData);

        // Return the imageUID
        return imageUID;

    }

    /**
     * Gets the data from a document and returns it as a map
     * Assumes there is only one document to return
     * Assumes the given document is an outer document
     *
     * @param docName The UID of the document in the outer collection.
     * @return The data in the document, or null if the document was not found
     */
    public Map<String, Object> getDataFromFirestore(String docName) {
        return this.getDataFromFirestore(docName, null, null);
    }


    // Chat GPT: Can you give me some java code within android to retrieve data from Firestore using a Completable future

    /**
     * Gets the data from a document and returns it as a map
     * Assumes there is only one document to return
     *
     * @param outerDocName  The UID of the document in the outer collection. If the other inputs are null, will return data from this document
     * @param innerCollName The name of the inner collection. If this is null, will return data from the outer collection
     * @param innerDocName  The UID of the document in the inner collection. If this is null, will return data from the outer collection
     * @return The data in the document, or null if the document was not found
     */
    public Map<String, Object> getDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Get the document reference for the outer/main collection
        docRef = collRef.document(outerDocName);

        // Get the document from firebase
        Task<DocumentSnapshot> task = null;
        if (innerCollName != null && innerDocName != null) {
            task = docRef.collection(innerCollName.name()).document(innerDocName).get();
        } else {
            task = docRef.get();
        }

        // Convert the task to a CompletableFuture
        CompletableFuture<DocumentSnapshot> future = toCompletableFuture(task);

        // Get the output
        DocumentSnapshot document = null;
        try {
            // Block until data is retrieved
            document = future.get();
            Log.d("GetFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetFromFirestore", "Task was interrupted: " + e.getMessage());
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetFromFirestore", "Error retrieving document: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Data was retrieved successfully
        Map<String, Object> data = null;
        if (document != null && document.exists()) {
            // Get the data from the query - assume only one document with this ID
            data = document.getData();

            // Attach the document id to the data
            assert data != null;
            data.put("UID", document.getId());

        } else {
            Log.e("GetFromFirestore", "The document does not exist");
        }

        // Return the received data
        return data;
    }

    /**
     * Gets an image from Firestore
     *
     * @param imageUID  The UID of the image
     * @param imageType The type of the image
     * @return A map containing the image and it's data
     */
    public Map<String, Object> getImageFromFirestore(String imageUID, ImageType imageType) {
        Blob imageBlob = null;

        // Find the correct image collection
        FirebaseAccess database = null;
        if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
            database = new FirebaseAccess(FirestoreAccessType.IMAGES);
        } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
            database = new FirebaseAccess(FirestoreAccessType.QRCODES);
        }

        // Get the image
        assert database != null;
        Map<String, Object> data = database.getDataFromFirestore(imageUID);

        // Check if the image actually exists
        if (data != null) {
            // Get the blob from the data and convert to an bitmap
            Bitmap image = blobToBitmap((Blob) Objects.requireNonNull(data.get("image")));

            // Add the bitmap to the returned data
            data.put("image", image);

            // Add the UID to the data
            data.put("UID", imageUID);

            // Return the data
            return data;
        } else {
            return null;
        }


    }


    /**
     * Deletes a document from Firestore
     * Deletes all inner collections before deleting the outer document
     * Assumes the document UID entered is a outer document
     * Assumes any linked objects were already deleted
     *
     * @param docName The UID of the outer document to delete
     */
    public void deleteDataFromFirestore(String docName) {
        this.deleteDataFromFirestore(docName, null, null);
    }

    /**
     * Deletes a document from Firestore.
     * Deletes all inner collections before deleting the outer document
     *
     * @param outerDocName  The UID of the document in the outer collection. If the other inputs are null, will delete this document
     * @param innerCollName The name of the inner collection. If this is null, will delete outer document
     * @param innerDocName  The UID of the document in the inner collection. If this is null, will delete outer document
     */
    public void deleteDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Get the document reference for the outer/main collection
        docRef = collRef.document(outerDocName);

        // If the document contains an inner collection, they need to be deleted first
        if (innerCollName == null || innerDocName == null) {
            // Get the list of inner collections
            ArrayList<FirebaseInnerCollection> innerCollList = new ArrayList<>();
            if (databaseType == FirestoreAccessType.EVENTS) {
                innerCollList.add(FirebaseInnerCollection.eventPosters);
                innerCollList.add(FirebaseInnerCollection.eventQRCodes);
                innerCollList.add(FirebaseInnerCollection.promoQRCodes);
                innerCollList.add(FirebaseInnerCollection.announcements);
            } else if (databaseType == FirestoreAccessType.USERS) {
                innerCollList.add(FirebaseInnerCollection.profilePictures);
            }

            // Delete all documents in the inner collection
            for (FirebaseInnerCollection currColl : innerCollList) {
                // Get the list of documents in the collection
                ArrayList<Map<String, Object>> allDocs = this.getAllDocuments(outerDocName, currColl);

                // Go through the list and delete them all
                if (allDocs != null) {
                    for (Map<String, Object> currDoc : allDocs) {
                        deleteDocumentFromFirestore(docRef.collection(currColl.name()).document((String) Objects.requireNonNull(currDoc.get("UID"))));
                    }
                }
            }

            // Delete the outer collection
            deleteDocumentFromFirestore(docRef);
        } else {  // This is an inner collection
            // Get a reference to the inner document
            DocumentReference innerDocRef = docRef.collection(innerCollName.name()).document(innerDocName);

            // Delete the document
            this.deleteDocumentFromFirestore(innerDocRef);
        }


    }

    /**
     * Deletes a single document from Firestore
     *
     * @param ref A reference to the document to delete
     */
    private void deleteDocumentFromFirestore(DocumentReference ref) {
        // Save the document id
        String docID = ref.getId();

        // Create the delete task
        Task<Void> task = null;
        task = ref.delete();

        // Convert the task to a CompletableFuture
        CompletableFuture<Void> future = toCompletableFuture(task);

        // Wait until delete is complete before returning
        try {
            // Wait for completion
            future.get();
            Log.d("DeleteFromFirestore", docID + "  has been deleted");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("DeleteFromFirestore", "Task was interrupted: " + e.getMessage());
            return;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("DeleteFromFirestore", "Error deleting document: " + docID + " - " + Objects.requireNonNull(e.getCause()).getMessage());
            return;
        }
    }

    /**
     * Gets the data of all documents in a collection
     * Assumes the collection is the main collection
     *
     * @return The list of document data
     */
    public ArrayList<Map<String, Object>> getAllDocuments() {
        return this.getAllDocuments(null, null);
    }


    /**
     * Gets the list of the data of all documents in a collection
     *
     * @param outerDocName  The UID of the document containing the inner collection, if null gets the data from the main collection
     * @param innerCollName The name of the inner collection, if null gets the data from the main collection
     * @return The list of document data
     */
    public ArrayList<Map<String, Object>> getAllDocuments(String outerDocName, FirebaseInnerCollection innerCollName) {
        // Create the get task
        Task<QuerySnapshot> task = null;

        // Get the documents from firebase
        if (innerCollName != null && outerDocName != null) {
            task = collRef.document(outerDocName).collection(innerCollName.name()).get();
        } else {
            task = collRef.get();
        }

        // Convert the task to a CompletableFuture
        CompletableFuture<QuerySnapshot> future = toCompletableFuture(task);

        // Get the output
        QuerySnapshot snapshot = null;
        try {
            // Block until data is retrieved
            snapshot = future.get();
            Log.d("GetAllFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetAllFromFirestore", "Task was interrupted: " + e.getMessage());
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetAllFromFirestore", "Error retrieving documents: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Data was retrieved successfully
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> docData = null;
        // If the data actually exists
        if (snapshot != null) {
            for (QueryDocumentSnapshot document : snapshot) {
                // Get the data from the document
                docData = document.getData();

                // Check if the document is the init document
                if (document.getId().equals("XXXX")) {
                    continue;
                }

                // Attach the UID
                docData.put("UID", document.getId());

                // Add to the array list
                data.add(docData);
            }
        } else {
            Log.e("GetAllFromFirestore", "The collection does not exist");
        }

        // Return the received data
        if (data.isEmpty()) {
            return null;
        } else {
            return data;
        }
    }

    /**
     * Deletes an image from Firestore
     * The reference to the image is also removed from its origin
     * The image link is also deleted
     *
     * @param imageUID  The UID of the image to delete
     * @param imageType The type of image (Ex. promoQRCCode)
     */
    public void deleteImageFromFirestore(String outerDocName, String imageUID, ImageType imageType) {
        // Check if the database is valid to delete images
        if (databaseType == FirestoreAccessType.IMAGES || databaseType == FirestoreAccessType.QRCODES) {
            // You shouldn't be calling this function on here
            Log.e("DeleteImage", "Incorrect database type");
            throw new IllegalArgumentException("Incorrect database type");
        }

        // Convert the ImageType to a FirebaseInnerCollection
        FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());

        // Delete the link to the image from the origin
        this.deleteDataFromFirestore(outerDocName, innerColl, imageUID);

        // Find the correct images database
        FirebaseAccess database = null;
        if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
            database = new FirebaseAccess(FirestoreAccessType.IMAGES);
        } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
            database = new FirebaseAccess(FirestoreAccessType.QRCODES);
        }

        // Delete the image from the database
        database.deleteDataFromFirestore(imageUID);

    }


    /**
     * Gets all the images contained in a Firestore inner collection
     *
     * @param outerDocName The UID of the document containing the inner collection, if null gets the data from the main collection
     * @param imageType    The the type of image you want to retrieve, if null gets the data from the main collection
     * @return The list of images and UIDs as a Map, or null if no images were found
     */
    public ArrayList<Map<String, Object>> getAllRelatedImagesFromFirestore(String outerDocName, ImageType imageType) {
        // Convert the ImageType to a FirebaseInnerCollection
        FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());

        // Get all the documents from the given collection
        ArrayList<Map<String, Object>> data = this.getAllDocuments(outerDocName, innerColl);


        // Get the images from Firebase Storage
        ArrayList<Map<String, Object>> outList = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> filePointer : data) {
                // Create the map
                HashMap<String, Object> outData = new HashMap<>();

                // Get the UID
                outData.put("UID", filePointer.get("UID"));

                // Get the image
                outData.put("image", this.getImageFromFirestore((String) filePointer.get("UID"), imageType));

                // Put the map in the list
                outList.add(outData);
            }
        }

        // Return the list of images
        if (outList.isEmpty()) {
            return null;
        } else {
            return outList;
        }
    }


    /**
     * Gets all the images of a specific type from Firestore
     *
     * @param imageType The type of images to get
     * @return The list of images and UIDs as a Map, or null if no images were found
     */
    public ArrayList<Map<String, Object>> getAllImagesFromFirestore(ImageType imageType) {
        // Find the correct image collection
        FirebaseAccess database = null;
        if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
            database = new FirebaseAccess(FirestoreAccessType.IMAGES);
        } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
            database = new FirebaseAccess(FirestoreAccessType.QRCODES);
        }

        // Get all the documents in the database
        assert database != null;
        ArrayList<Map<String, Object>> data = database.getAllDocuments();

        // Go through the documents and add the ones that match the given type to the output list
        ArrayList<Map<String, Object>> outList = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> potentialImage : data) {
                // Check if the image matches the type
                if (Objects.equals((String) potentialImage.get("type"), imageType.name())) {
                    // Convert the image to a bitmap and attach it to the data
                    potentialImage.put("image", blobToBitmap((Blob) Objects.requireNonNull(potentialImage.get("image"))));

                    // Add the image to the output
                    outList.add(potentialImage);
                }
            }
        }

        // Return the list of images
        if (outList.isEmpty()) {
            return null;
        } else {
            return outList;
        }

    }

    /**
     * This function is callable from any access
     * DELETES ALL DOCUMENTS ACROSS ALL COLLECTIONS
     */
    public void deleteAllDataInFirestore() {
        // Create an array list of all collections
        ArrayList<FirebaseAccess> databases = new ArrayList<>();

        // Add collections to the list
        for (FirestoreAccessType access : FirestoreAccessType.values()) {
            databases.add(new FirebaseAccess(access));
        }

        // For each collection
        for (FirebaseAccess database : databases) {
            // Find all documents
            ArrayList<Map<String, Object>> data = database.getAllDocuments();

            // Check if the document exists
            if (data != null) {
                // Delete each document
                for (Map<String, Object> document : data) {
                    database.deleteDataFromFirestore((String) document.get("UID"));
                }
            }

            // Re-initialize the collections
            Map<String, Object> newData = new HashMap<>();
            database.storeDataInFirestore("XXXX", newData);


        }



    }
    public CompletableFuture<ArrayList<Event>> getAllEvents () {
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Event> events = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    events.add(document.toObject(Event.class));
                }
                futureEvents.complete(events);
            } else {
                futureEvents.completeExceptionally(task.getException());
            }
        });
        return futureEvents;
    }
}
