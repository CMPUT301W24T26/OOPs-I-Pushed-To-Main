/*
 Example Use:
     user = new User("USER-9DRH1BAQZQMGZJEZFMGL", new User.DataLoadedListener() {
        @Override
        public void onDataLoaded() {
            AfterDone();
        }
    });
 */

package com.oopsipushedtomain;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oopsipushedtomain.ProfileActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class defines and represents a user
 * It also includes all database accesses for user functions
 * <p>
 * Outstanding Issues:
 * - Need to implement more error checking
 * - Need to implement a delete function
 *
 * @author Matteo Falsetti
 * @version 1.0
 * @see Event
 * @see ProfileActivity
 */
public class User {
    // User parameters
    /**
     * The UID of the user
     */
    private String uid;
    /**
     * The address of the user
     * It says hi when you create a new account
     */
    private String address = "Hello";
    /**
     * The birthday of the user
     */
    private Date birthday = null;
    /**
     * The email of the user
     */
    private String email = null;
    /**
     * The homepage of the user
     */
    private String homepage = null;
    /**
     * The full name of the user
     */
    private String name = null;
    /**
     * The nickname of the user
     */
    private String nickname = null;
    /**
     * The phone number of the user
     */
    private String phone = null;

    /**
     * The UID of the user's profile picture
     */
    private String imageUID = null;
    /**
     * The Firebase Installation ID (fid)
     */
    private String fid = null;

    // Announcements
    /**
     * The array list to store annoncements for this user
     */
    private ArrayList<String> announcementsList;

    // Database parameters
    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db;
    /**
     * A reference to the users collection
     */
    private CollectionReference userRef;
    /**
     * A reference to the doucment for this user
     */
    private DocumentReference userDocRef;
    /**
     * A reference to the events collection for each user
     */
    private CollectionReference userEventRef;


    // Firebase storage
    /**
     * A reference to Firebase Storage
     */
    private FirebaseStorage storage;
    /**
     * A reference to the storage pool for images
     */
    private StorageReference storageRef;

    /**
     * Interface for checking when data is loaded into the user
     */
    public interface DataLoadedListener {
        /**
         * Call back for when data is loaded into the user
         */
        void onDataLoaded();
    }

    /**
     * Interface for checking when a new user is created
     */
    public interface UserCreatedListener {
        /**
         * Callback for passing back the created user when creating a new user
         * @param user The new user that was created
         */
        void onDataLoaded(User user);
    }

    /**
     * Interface for checking when the image is loaded from the database
     */
    public interface OnBitmapReceivedListener {
        /**
         * Callback for passing back the user's profile picture when it was received
         * @param bitmap The received bitmap
         */
        void onBitmapReceived(Bitmap bitmap);
    }

    /**
     * Initializes the database parameters for accessing firestore
     */
    public void InitDatabase() {
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }


    /**
     * Generates a new user and uploads them to the database
     * Instantiates all parameters to null. They need to be set later
     *
     * @param listener The listener for determining when the transfer is complete
     */
    public User(UserCreatedListener listener) {
        // Initialize the database
        InitDatabase();

        // Create array list
        announcementsList = new ArrayList<>();

        // Get the user id of a new document
        uid = userRef.document().getId().toUpperCase();
        uid = "USER-" + uid;

        // Get the UID for an image
        imageUID = userRef.document().getId().toUpperCase();
        imageUID = "IMGE-" + imageUID;

        Log.e("asdf", "test");

        // Create a hash map for all string variables and set all fields to null
        HashMap<String, Object> data = new HashMap<>();
        data.put("address", address);
        data.put("birthday", birthday);
        data.put("email", email);
        data.put("homepage", homepage);
        data.put("name", name);
        data.put("nickname", nickname);
        data.put("phone", phone);
        data.put("profileImage", imageUID);
        data.put("fid", fid);


        // Create a new document and set all parameters
        userRef.document(uid).set(data);

        // Set the document reference to this document
        userDocRef = userRef.document(uid);


        Log.e("asdf2", "test");
        // Create the inner collection for events
        userEventRef = userDocRef.collection("events");

        // Create empty data to force creation of the document
        HashMap<String, String> emptyData = new HashMap<>();

        // Add the documents
        userEventRef.document("checkedin").set(emptyData);
        userEventRef.document("created").set(emptyData);
        userEventRef.document("signedup").set(emptyData);

        // Notify complete
        listener.onDataLoaded(this);
    }

    /**
     * Creates an instance of the new user class given a UID.
     * Loads the data from the database
     *
     * @param userID   The UID of the user to find in the database
     * @param listener Listener for checking when file transfer is complete
     */
    public User(String userID, DataLoadedListener listener) {
        // Initialize database
        InitDatabase();

        // Create array list
        announcementsList = new ArrayList<>();

        // Set the document ID
        uid = userID;
        userDocRef = userRef.document(uid);

        // Set the events document id
        userEventRef = userDocRef.collection("events");

        // Set the fields of the class
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateAllDataFields(listener);
                ;
            }
        }).start();

    }

    /**
     * Constructs a User instance directly from a map of data, typically retrieved from a database.
     * This constructor is ideal for batch loading users when the data is already available,
     * avoiding the need to individually query each user's data from the database.
     * It initializes user attributes directly from the provided map, which should contain
     * keys corresponding to user attribute names and values matching the attribute types.
     *
     * @param userData A map containing user data, where each key corresponds to a user attribute name
     *                 and the associated value is the attribute value. The map is expected to have
     *                 entries for all user attributes, including uid, address, birthday, email,
     *                 homepage, name, nickname, phone, profileImage, and fid.
     *                 The birthday should be provided as a Timestamp object, which will be converted to a Date.
     *                 If any attributes are missing or null, the corresponding user attribute will be initialized
     *                 to a default state (null for objects, empty string for strings, etc.).
     */
    public User(Map<String, Object> userData) {
        InitDatabase(); // Initialize database references

        // Directly assign attribute values from the map, with type checking and conversion as necessary
        this.uid = (String) userData.get("uid");
        this.address = (String) userData.get("address");

        Object birthdayObj = userData.get("birthday");
        if (birthdayObj instanceof Timestamp) {
            this.birthday = ((Timestamp) birthdayObj).toDate();
        } else {
            this.birthday = null; // Set to null if the birthday data is not in Timestamp format
        }

        this.email = (String) userData.get("email");
        this.homepage = (String) userData.get("homepage");
        this.name = (String) userData.get("name");
        this.nickname = (String) userData.get("nickname");
        this.phone = (String) userData.get("phone");
        this.imageUID = (String) userData.get("profileImage");
        this.fid = (String) userData.get("fid");

        // Note: Since this constructor does not load data asynchronously from the database,
        // there is no need for a DataLoadedListener parameter or callback mechanism here.
    }


    /**
     * Updates all fields in the class
     * Needs to be called before getting any data
     *
     * @param listener Listener for checking when data transfer is complete
     */
    public void UpdateAllDataFields(DataLoadedListener listener) {
        // Get the data in the document
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the data from the query - there should only be 1 document
                        Map<String, Object> data = document.getData();

                        // Get Single element fields
                        assert data != null;
                        address = (String) data.get("address");
                        Object birthdayObject = data.get("birthday");
                        if (birthdayObject instanceof Timestamp) {
                            birthday = ((Timestamp) birthdayObject).toDate();
                        } else {
                            // Handle the case where birthday is null or not a Timestamp. You could set a default value or leave it as null
                            birthday = null; // or set a default date if appropriate
                        }
                        email = (String) data.get("email");
                        homepage = (String) data.get("homepage");
                        name = (String) data.get("name");
                        nickname = (String) data.get("nickname");
                        phone = (String) data.get("phone");
                        imageUID = (String) data.get("profileImage");
                        fid = (String) data.get("fid");

                        // Load the announcements
                        // ChatGPT: How do I load an array list from firebase
                        try {
                            List<Object> rawList = (List<Object>) data.get("announcements");
                            Log.d("Firebase", "Array list loading");

                            // Convert each element a string
                            if (rawList != null) {
                                for (Object item : rawList) {
                                    if (item instanceof String) {
                                        announcementsList.add((String) item);
                                    }
                                }
                            }
                        } catch (ClassCastException e) {
                            Log.d("Firebase", "Array list loading failed");
                        }


                        // Inform complete
                        Log.d("Firebase", "Data Loaded");
                        listener.onDataLoaded();

                    } else {
                        Log.d("Firebase", "No such document");
                    }
                } else {
                    Log.d("Firebase Failure", "get failed with ", task.getException());
                }
            }
        });
    }


    /**
     * Updates the user's address
     *
     * @param address The address of the user
     */
    public void setAddress(String address) {
        // Update in the class
        this.address = address;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("address", this.address);
        userDocRef.update(data);
    }

    /**
     * Updates the user's birthday
     *
     * @param birthday The birthday of the user
     */
    public void setBirthday(Date birthday) {
        // Update in the class
        this.birthday = birthday;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("birthday", this.birthday);
        userDocRef.update(data);
    }

    /**
     * Updates the user's email
     *
     * @param email The email of the user
     */
    public void setEmail(String email) {
        // Update in the class
        this.email = email;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("email", this.email);
        userDocRef.update(data);
    }

    /**
     * Updates the user's homepage
     *
     * @param homepage the homepage of the user
     */
    public void setHomepage(String homepage) {
        // Update in the class
        this.homepage = homepage;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("homepage", this.homepage);
        userDocRef.update(data);
    }

    /**
     * Updates the user's name
     *
     * @param name The name of the user
     */
    public void setName(String name) {
        // Update in the class
        this.name = name;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        userDocRef.update(data);
    }

    /**
     * Updates the user's nickname
     *
     * @param nickname The nickname of the user
     */
    public void setNickname(String nickname) {
        // Update in the class
        this.nickname = nickname;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("nickname", this.nickname);
        userDocRef.update(data);
    }

    /**
     * Updates the user's phone number
     *
     * @param phone The phone number of the user
     */
    public void setPhone(String phone) {
        // Update in the class
        this.phone = phone;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("phone", this.phone);
        userDocRef.update(data);
    }

    /**
     * Updates the user's FID
     *
     * @param fid The fid of the installation
     */
    public void setFid(String fid) {
        // Update in the class
        this.fid = fid;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("fid", this.fid);
        userDocRef.update(data);
    }


    // ChatGPT: How can you upload an image to firebase?

    /**
     * Sets the user's profile image in the database
     *
     * @param profileImage The image to store as a bitmap
     */
    public void setProfileImage(Bitmap profileImage) {
        if (imageUID == null || imageUID.isEmpty()) {
            Log.e("Firebase Storage", "Invalid imageUID, cannot upload image.");
            return;
        }
        // Convert the bitmap to PNG for upload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to firebase
        UploadTask uploadTask = storageRef.child(imageUID).putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d("Firebase Storage", "Image upload successful");
        }).addOnFailureListener(exception -> {
            Log.d("Firebase Storage", "Image upload failed");
        });
    }
    // ChatGPT: How can you delete an image to firebase storage?

    /**
     * Deletes the user's profile image in the database
     */
    public void deleteProfileImage() {
        // Check if there is an image to delete
        if (imageUID == null || imageUID.isEmpty()) {
            Log.d("Firebase Storage", "No image to delete.");
            return;
        }

        // Delete the image from Firebase Storage
        StorageReference fileRef = storageRef.child(imageUID);
        fileRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("Firebase Storage", "Image successfully deleted");
        }).addOnFailureListener(e -> {
            Log.d("Firebase Storage", "Error deleting image", e);
        });
    }

    /**
     * Gets the UID for the user
     *
     * @return The UID of the user
     */
    public String getUid() {
//        this.UpdateAllDataFields();
        return uid;
    }

    /**
     * Gets the address of the user
     *
     * @return The address of the user
     */
    public String getAddress() {
//        this.UpdateAllDataFields();
        return address;
    }

    /**
     * Gets the birthday of the user
     *
     * @return The birthday of the user
     */
    public Date getBirthday() {
//        this.UpdateAllDataFields();
        return birthday;
    }

    /**
     * Gets the email of the user
     *
     * @return The email of the user
     */
    public String getEmail() {
//        this.UpdateAllDataFields();
        return email;
    }

    /**
     * Gets the homepage of the user
     *
     * @return The homepage of the user
     */
    public String getHomepage() {
//        this.UpdateAllDataFields();
        return homepage;
    }

    /**
     * Gets the name of the user
     *
     * @return The name of the user
     */
    public String getName() {
//        this.UpdateAllDataFields();
        return name;
    }

    /**
     * Gets the nickname of the user
     *
     * @return The nickname of the user
     */
    public String getNickname() {
//        this.UpdateAllDataFields();
        return nickname;
    }

    /**
     * Gets the phone number of the user
     *
     * @return The phone number of the user
     */
    public String getPhone() {
//        this.UpdateAllDataFields();
        return phone;
    }

    /**
     * Gets the image string of the user
     *
     * @return The image string of the user
     */
    public String getImageUID() {
        return imageUID;
    }


    /**
     * Gets the FID of the user
     *
     * @return The FID of the user
     */
    public String getFid() {
//        this.UpdateAllDataFields();
        return fid;
    }


    // ChatGPT: Now i want to do the reverse and load the image and convert it back to a bitmap

    /**
     * Gets the users profile image from the database.
     * The Bitmap profile image is passed out through the listener
     * @param listener The listener to to see when data transfer is finished
     */
    public void getProfileImage(OnBitmapReceivedListener listener) {
        if (imageUID == null || imageUID.isEmpty()) {
            Log.d("User", "No imageUID available for user: " + uid);
            // Call the listener with a null or default bitmap
            listener.onBitmapReceived(null); // or pass a default Bitmap
            return;
        }

        StorageReference profileImageRef = storageRef.child(imageUID);
        final long ONE_MEGABYTE = 1024 * 1024;
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            listener.onBitmapReceived(bitmap);
        }).addOnFailureListener(e -> {
            Log.e("User", "Failed to load profile image for user: " + uid, e);
            listener.onBitmapReceived(null); // or pass a default Bitmap on failure
        });
    }

    /**
     * Gets the array list containing the announcements
     *
     * @return The announcements array list
     */
    public ArrayList<String> getAnnouncementsList() {
        return announcementsList;
    }


    /**
     * Checks a user into the specified event.
     * It will create a new entry if it does not exist already
     *
     * @param eventID The UID of the event
     */
    public void checkIn(String eventID) {
        // Check to see if the user has checked in before
        DocumentReference checkInRef = userEventRef.document("checkedin");
        checkInRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                // Check if document exists
                if (document.exists()) {
                    // Check if this event is already there, if it is, increment that count
                    if (document.get(eventID) != null) {
                        // Get the field in the map
                        String countField = eventID + ".count";

                        // Update the count
                        checkInRef.update(countField, FieldValue.increment(1));

                    } else {
                        // Event does not already exist, make a new event
                        HashMap<String, Object> data = new HashMap<>();
                        HashMap<String, Object> internalMap = new HashMap<>();

                        // Add the data to the internal map
                        internalMap.put("count", 1);
                        internalMap.put("date-time", new Date());
                        internalMap.put("location", new GeoPoint(0, 0));

                        // Add the map to the document
                        data.put(eventID, internalMap);
                        checkInRef.update(data);
                    }

                    // Subscribe the user to notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(eventID);

                } else {
                    Log.d("Firebase Check In", "No such document");
                }

            }
        });

    }
}
