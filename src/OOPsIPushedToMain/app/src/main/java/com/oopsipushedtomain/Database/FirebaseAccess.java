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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class for all Firebase accesses
 * Has methods for accessing both storage and Firestore database
 * Methods will return futures, use .thenAccept or .thenApply to handle the output!!!
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

    private ExecutorService executorService = Executors.newFixedThreadPool(4);


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

    // ChatGPT: Can I turn a callable into a completable future

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

    // ChatGPT: What is the best way to convert a bitmap into a string
    // ChatGPT: Is there a more compressed encoding? Can Firestore store a byte array?

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
     * Generates a new UID for the specified database type and inner collection
     * @param outerColl The outer collection
     * @param innerCollName The inner collection
     * @return The generated UID
     */
    private static String generateNewUID(FirestoreAccessType outerColl, FirebaseInnerCollection innerCollName) {
        // Get a database reference
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // If the inner collection is null, generate for the outer collection
        if (innerCollName == null) {
            switch (outerColl) {
                case QRCODES:
                    return "QRCD-" + db.collection("qrcodes").document().getId().toUpperCase();
                case USERS:
                    return "USER-" + db.collection("users").document().getId().toUpperCase();
                case IMAGES:
                    return "IMGE-" + db.collection("images").document().getId().toUpperCase();
                case EVENTS:
                    return "EVNT-" + db.collection("events").document().getId().toUpperCase();
            }
        } else {    // Inner collection is given
            String UIDBase;
            switch (outerColl) {
                // Events
                case EVENTS:
                    UIDBase = db.collection("events").document().collection(innerCollName.name()).document().getId().toUpperCase();
                    // Get the correct prefix
                    switch (innerCollName) {
                        case announcements:
                            return "ANMT-" + UIDBase;
                        case checkInCoords:
                            return "COOR-" + UIDBase;
                        case eventPosters:
                            return "IMGE-" + UIDBase;
                        case eventQRCodes:
                        case promoQRCodes:
                            return "QRCD-" + UIDBase;
                        default:
                            throw new IllegalArgumentException("Invalid database combination!!");
                    }

                    // Users
                case USERS:
                    UIDBase = db.collection("users").document().collection(innerCollName.name()).document().getId().toUpperCase();
                    // Get the correct prefix
                    switch (innerCollName) {
                        case profilePictures:
                            return "IMGE-" + UIDBase;
                        default:
                            throw new IllegalArgumentException("Invalid database combination!!");
                    }

                case IMAGES:
                    throw new IllegalArgumentException("Images don't have any inner collections!!");
                case QRCODES:
                    throw new IllegalArgumentException("QR codes don't have any inner collections!!");
            }
        }

        // If the inputs are invalid, throw an error
        throw new IllegalArgumentException("Invalid database for generate UID, use the appropriate store instead");
    }

    /**
     * Converts a callable into a completable future
     *
     * @param callable The callable to convert
     * @param <T>      The return type of the callable
     * @return The completable future
     */
    public <T> CompletableFuture<T> callableToCompletableFuture(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();

        // Submit the Callable to the executor and complete the future when the Callable task finishes
        executorService.submit(() -> {
            try {
                T result = callable.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
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
        boolean isValidDatabase = false;
        // Check for a valid combination of outer and inner collection
        switch (this.databaseType) {
            case QRCODES:
            case IMAGES:
                isValidDatabase = innerCollName == null;
                break;
            case USERS:
                if (innerCollName != null) {
                    switch (innerCollName) {
                        case checkInCoords:
                        case announcements:
                        case promoQRCodes:
                        case eventPosters:
                        case eventQRCodes:
                            // Wrong collection
                            break;
                        case signedUpEvents:
                        case checkedInEvents:
                        case profilePictures:
                            // Require UID
                            isValidDatabase = innerDocName != null;
                    }
                } else isValidDatabase = innerDocName == null;
                break;
            case EVENTS:
                if (innerCollName != null) {
                    switch (innerCollName) {
                        case eventPosters:
                        case eventQRCodes:
                        case promoQRCodes:
                            // Require inner doc UID to be specified
                            isValidDatabase = innerDocName != null;
                            break;
                        case checkInCoords:
                        case announcements:
                            // Can generate doc UID
                            isValidDatabase = true;
                            break;
                        case signedUpEvents:
                        case checkedInEvents:
                        case profilePictures:
                            // Wrong collection
                            break;

                    }
                } else isValidDatabase = innerDocName == null;
                break;
            default:
                // Default is false
        }

        if (!isValidDatabase) {
            throw new IllegalArgumentException("Invalid database to store in!!");
        }

        // If the outerDocName is not given, make a new one
        if (outerDocName == null) {
            outerDocName = generateNewUID(this.databaseType, null);
        }

        // If the inner document name is not specified, create a new one
        // Essentially a special case for announcements
        if (innerDocName == null && innerCollName != null) {
            innerDocName = generateNewUID(this.databaseType, innerCollName);
        } else if (innerCollName == null && innerDocName != null) {
            throw new IllegalArgumentException("Inner collection must be specified!");
        }


        String finalOuterDocName = outerDocName;
        String finalInnerDocName = innerDocName;
        // Create the callable for storing the data
        Callable<Void> firestoreTask = () -> {
            docRef = collRef.document(finalOuterDocName);

            // Store the data
            Task<Void> task = null;
            if (innerCollName != null && finalInnerDocName != null) {
                docRef.collection(innerCollName.name()).document(finalInnerDocName).set(data);
            } else {
                docRef.set(data);
            }

            return null;
        };

        // Execute the store, no need to return the future (probably)
        executorService.submit(firestoreTask);

        // Return the UID of the documents
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
        assert attachedTo != null;
        assert imageType != null;

        boolean isValidDatabase = false;
        // Check for a valid combination of outer and inner collection
        switch (this.databaseType) {
            case QRCODES:
            case IMAGES:
                break;
            case USERS:
                switch (imageType) {
                    case profilePictures:
                        isValidDatabase = true;
                    case promoQRCodes:
                    case eventQRCodes:
                    case eventPosters:
                        break;
                }
            case EVENTS:
                switch (imageType){
                    case profilePictures:
                        break;
                    case eventQRCodes:
                    case eventPosters:
                    case promoQRCodes:
                        isValidDatabase = true;
                }
        }


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
            // Convert the imageType to an inner collection
            FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());
            imageUID = generateNewUID(this.databaseType, null);
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

        // Create the callable
        String finalImageUID = imageUID;
        Map<String, Object> finalImageData = imageData;
        Callable<Void> firestoreTask = () -> {
            // The access to the correct image database
            FirebaseAccess imageAccess = null;

            // Get the correct database to store the image and store the origin in the correct inner collection
            switch (this.databaseType) {
                case EVENTS:
                    switch (imageType) {
                        case eventPosters:
                            imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                            this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventPosters, finalImageUID, originData);
                            break;
                        case eventQRCodes:
                            this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventQRCodes, finalImageUID, originData);
                            imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                            break;
                        case promoQRCodes:
                            this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.promoQRCodes, finalImageUID, originData);
                            imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                            break;
                    }
                    break;
                case USERS:
                    this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.profilePictures, finalImageUID, originData);
                    imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                    break;
            }

            // Store the image in the database
            assert imageAccess != null;
            imageAccess.storeDataInFirestore(finalImageUID, finalImageData);

            return null;
        };

        // Execute the task, no need to return the future (probably)
        executorService.submit(firestoreTask);

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
    public CompletableFuture<Map<String, Object>> getDataFromFirestore(String docName) {
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
    public CompletableFuture<Map<String, Object>> getDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Get the document reference for the outer/main collection
        docRef = collRef.document(outerDocName);

        // Create the callable to get the data
        Callable<Map<String, Object>> firestoreTask = () -> {
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

            return data;
        };


        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }

    /**
     * Gets an image from Firestore
     *
     * @param imageUID  The UID of the image
     * @param imageType The type of the image
     * @return A map containing the image and it's data
     */
    public CompletableFuture<Map<String, Object>> getImageFromFirestore(String imageUID, ImageType imageType) {
        Blob imageBlob = null;

        // Find the correct image collection
        FirebaseAccess database;
        if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
            database = new FirebaseAccess(FirestoreAccessType.IMAGES);
        } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
            database = new FirebaseAccess(FirestoreAccessType.QRCODES);
        } else {
            database = null;
        }

        // Create the callable
        Callable<Map<String, Object>> firestoreTask = () -> {
            // Get the image
            assert database != null;
            Future<Map<String, Object>> futureData = database.getDataFromFirestore(imageUID);

            Map<String, Object> data = new HashMap<>();
            try {
                // Block until data is retrieved
                data = futureData.get();
                Log.d("GetImageFromFirestore", "Data has been received");
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
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);

    }


    /**
     * Deletes a document from Firestore
     * Deletes all inner collections before deleting the outer document
     * Assumes the document UID entered is a outer document
     * Assumes any linked objects were already deleted
     *
     * @param docName The UID of the outer document to delete
     */
    public CompletableFuture<Void> deleteDataFromFirestore(String docName) {
        return this.deleteDataFromFirestore(docName, null, null);
    }

    /**
     * Deletes a document from Firestore.
     * Deletes all inner collections before deleting the outer document
     *
     * @param outerDocName  The UID of the document in the outer collection. If the other inputs are null, will delete this document
     * @param innerCollName The name of the inner collection. If this is null, will delete outer document
     * @param innerDocName  The UID of the document in the inner collection. If this is null, will delete outer document
     */
    public CompletableFuture<Void> deleteDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Create the callable
        Callable<Void> firestoreTask = () -> {

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
                    CompletableFuture<ArrayList<Map<String, Object>>> futureDocs = this.getAllDocuments(outerDocName, currColl);
                    ArrayList<Map<String, Object>> allDocs = futureDocs.get();

                    // Go through the list and delete them all
                    if (allDocs != null) {
                        for (Map<String, Object> currDoc : allDocs) {
                            deleteDocumentFromFirestore(docRef.collection(currColl.name()).document((String) Objects.requireNonNull(currDoc.get("UID")))).get();
                        }
                    }
                }

                // Delete the outer collection
                deleteDocumentFromFirestore(docRef).get();
            } else {  // This is an inner collection
                // Get a reference to the inner document
                DocumentReference innerDocRef = docRef.collection(innerCollName.name()).document(innerDocName);

                // Delete the document
                this.deleteDocumentFromFirestore(innerDocRef).get();
            }

            return null;
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }

    /**
     * Deletes a single document from Firestore
     *
     * @param ref A reference to the document to delete
     */
    private CompletableFuture<Void> deleteDocumentFromFirestore(DocumentReference ref) {
        // Create the callable
        Callable<Void> firestoreTask = () -> {

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
                return null;
            } catch (InterruptedException e) {
                // Handle the interrupted exception
                Thread.currentThread().interrupt();
                Log.e("DeleteFromFirestore", "Task was interrupted: " + e.getMessage());
                return null;
            } catch (ExecutionException e) {
                // Handle any other exception
                Log.e("DeleteFromFirestore", "Error deleting document: " + docID + " - " + Objects.requireNonNull(e.getCause()).getMessage());
                return null;
            }
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }

    /**
     * Gets the data of all documents in a collection
     * Assumes the collection is the main collection
     *
     * @return The list of document data
     */
    public CompletableFuture<ArrayList<Map<String, Object>>> getAllDocuments() {
        return this.getAllDocuments(null, null);
    }


    /**
     * Gets the list of the data of all documents in a collection
     *
     * @param outerDocName  The UID of the document containing the inner collection, if null gets the data from the main collection
     * @param innerCollName The name of the inner collection, if null gets the data from the main collection
     * @return The list of document data
     */
    public CompletableFuture<ArrayList<Map<String, Object>>> getAllDocuments(String outerDocName, FirebaseInnerCollection innerCollName) {
        // Create the callable
        Callable<ArrayList<Map<String, Object>>> firestoreTask = () -> {
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
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }

    /**
     * Deletes an image from Firestore
     * The reference to the image is also removed from its origin
     * The image link is also deleted
     *
     * @param imageUID  The UID of the image to delete
     * @param imageType The type of image (Ex. promoQRCCode)
     */
    public CompletableFuture<Void> deleteImageFromFirestore(String outerDocName, String imageUID, ImageType imageType) {
        // Check if the database is valid to delete images
        if (databaseType == FirestoreAccessType.IMAGES || databaseType == FirestoreAccessType.QRCODES) {
            // You shouldn't be calling this function on here
            Log.e("DeleteImage", "Incorrect database type");
            throw new IllegalArgumentException("Incorrect database type");
        }

        // Convert the ImageType to a FirebaseInnerCollection
        FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());

        // Create the callable
        Callable<Void> firestoreTask = () -> {
            // Delete the link to the image from the origin
            this.deleteDataFromFirestore(outerDocName, innerColl, imageUID).get();

            // Find the correct images database
            FirebaseAccess database = null;
            if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
                database = new FirebaseAccess(FirestoreAccessType.IMAGES);
            } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
                database = new FirebaseAccess(FirestoreAccessType.QRCODES);
            }

            // Delete the image from the database
            database.deleteDataFromFirestore(imageUID).get();

            return null;
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);


    }


    /**
     * Gets all the images contained in a Firestore inner collection
     *
     * @param outerDocName The UID of the document containing the inner collection, if null gets the data from the main collection
     * @param imageType    The the type of image you want to retrieve, if null gets the data from the main collection
     * @return The list of images and UIDs as a Map, or null if no images were found
     */
    public CompletableFuture<ArrayList<Map<String, Object>>> getAllRelatedImagesFromFirestore(String outerDocName, ImageType imageType) {
        // Convert the ImageType to a FirebaseInnerCollection
        FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());

        // Create callable
        Callable<ArrayList<Map<String, Object>>> firestoreTask = () -> {

            // Get all the documents from the given collection
            ArrayList<Map<String, Object>> data = this.getAllDocuments(outerDocName, innerColl).get();


            // Get the images from Firebase Storage
            ArrayList<Map<String, Object>> outList = new ArrayList<>();
            if (data != null) {
                for (Map<String, Object> filePointer : data) {
                    // Create the map
                    HashMap<String, Object> outData = new HashMap<>();

                    // Get the UID
                    outData.put("UID", filePointer.get("UID"));

                    // Get the image
                    outData.put("image", this.getImageFromFirestore((String) filePointer.get("UID"), imageType).get());

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
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }


    /**
     * Gets all the images of a specific type from Firestore
     *
     * @param imageType The type of images to get
     * @return The list of images and UIDs as a Map, or null if no images were found
     */
    public CompletableFuture<ArrayList<Map<String, Object>>> getAllImagesFromFirestore(ImageType imageType) {
        // Find the correct image collection
        FirebaseAccess database;
        if (imageType == ImageType.eventPosters || imageType == ImageType.profilePictures) {
            database = new FirebaseAccess(FirestoreAccessType.IMAGES);
        } else if (imageType == ImageType.eventQRCodes || imageType == ImageType.promoQRCodes) {
            database = new FirebaseAccess(FirestoreAccessType.QRCODES);
        } else {
            database = null;
        }

        // Create the callable
        Callable<ArrayList<Map<String, Object>>> firestoreTask = () -> {

            // Get all the documents in the database
            assert database != null;
            ArrayList<Map<String, Object>> data = database.getAllDocuments().get();

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
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);

    }

    /**
     * This function is callable from any access
     * DELETES ALL DOCUMENTS ACROSS ALL COLLECTIONS
     */
    public CompletableFuture<Void> deleteAllDataInFirestore() {
        // Create an array list of all collections
        ArrayList<FirebaseAccess> databases = new ArrayList<>();

        // Add collections to the list
        for (FirestoreAccessType access : FirestoreAccessType.values()) {
            databases.add(new FirebaseAccess(access));
        }

        // Create the callable
        Callable<Void> firestoreTask = () -> {
            // For each collection
            for (FirebaseAccess database : databases) {
                // Find all documents
                ArrayList<Map<String, Object>> data = database.getAllDocuments().get();

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

            return null;
        };

        // Return the future
        return callableToCompletableFuture(firestoreTask);
    }


}
