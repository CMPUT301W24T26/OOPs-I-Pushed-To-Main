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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import org.checkerframework.checker.units.qual.C;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    FirebaseAccess database;

    // User parameters
    /**
     * The permission level of the user
     */
    boolean isAdmin = false;
    /**
     * The UID of the user
     */
    private String uid;
    /**
     * The address of the user
     * It says Bye when you create a new account
     */
    private String address = "Bye"; // Changing this breaks a test
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
    private String profileImageUID = null;
    /**
     * The user's profile picture
     */
    private Bitmap profileImage = null;
    /**
     * The Firebase Installation ID (fid)
     */
    private String fid = null;
    /**
     * Whether the data in the class is current
     */
    private boolean dataIsCurrent = false;
    /**
     * Whether the user has geolocation enabled
     */
    private boolean geolocation = false;

    /**
     * Generates a new user
     * Sets the database reference only, use User.createNewObject to create a new object
     */
    private User() {
        // Create a reference to the users database
        database = new FirebaseAccess(FirestoreAccessType.USERS);

    }

    /**
     * Creates a new User object
     *
     * @return A completable future to get the user
     */
    public static CompletableFuture<User> createNewObject() {
        return User.createNewObject(null);
    }

    /**
     * Creates a new User object
     *
     * @param UID The UID of the user to create. If null, creates a new one
     * @return A completable future to get the user
     */
    public static CompletableFuture<User> createNewObject(String UID) {
        User createdUser;
        CompletableFuture<User> future = new CompletableFuture<>();

        // If the UID is null, create a new User
        if (UID == null) {
            createdUser = new User();

            // Create a hash map for all string variables and set all fields to null
            HashMap<String, Object> data = new HashMap<>();
            data.put("address", "Bye");
            data.put("birthday", null);
            data.put("email", null);
            data.put("homepage", null);
            data.put("name", null);
            data.put("nickname", null);
            data.put("phone", null);
            data.put("fid", null);
            data.put("geolocation", false);
            data.put("isAdmin", false);

            // Upload the data to the database
            Map<String, Object> storeReturn = createdUser.database.storeDataInFirestore(null, data);

            // Set the UID
            createdUser.uid = (String) storeReturn.get("outer");

            // Wait for the data to be stored before completing the future
            CompletableFuture<Void> storeFuture = (CompletableFuture<Void>) storeReturn.get("future");
            assert storeFuture != null;
            storeFuture.thenAccept(result -> {
                // Data is current
                createdUser.dataIsCurrent = true;
                future.complete(createdUser);
            });

            // Return the future
            return future;
        } else {
            // Otherwise create a new user and load in the data
            createdUser = new User();

            // Set the UID of the user
            createdUser.uid = UID;

            // Update the user details
            createdUser.updateUserFromDatabase().thenAccept(result -> {
                // Data is current
                createdUser.dataIsCurrent = true;
                future.complete(createdUser);
            });

            // Return the future
            return future;
        }

    }


    /**
     * Stores the received Firestore data in the class
     *
     * @param data The Firestore data
     */
    private void parseFirestoreData(Map<String, Object> data) {
        // Store the data in the class
        this.address = (String) data.get("address");
        if (data.get("birthday") != null) {
            this.birthday = ((Timestamp) Objects.requireNonNull(data.get("birthday"))).toDate();
        }
        this.email = (String) data.get("email");
        this.homepage = (String) data.get("homepage");
        this.name = (String) data.get("name");
        this.nickname = (String) data.get("nickname");
        this.phone = (String) data.get("phone");
        this.fid = (String) data.get("fid");
        if (data.get("geolocation") != null) {
            this.geolocation = (Boolean) data.get("geolocation");
        }
        if (data.get("isAdmin") != null) {
            this.isAdmin = (Boolean) data.get("isAdmin");
        }

    }


    /**
     * Updates the user from the database
     *
     * @return A completable future for waiting for data to be received
     */
    public CompletableFuture<Void> updateUserFromDatabase() {
        // Create the future to return
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Check if the data is already current
        if (!dataIsCurrent) {
            // Get the data from firestore
            database.getDataFromFirestore(this.uid).thenAccept(data -> {
                // Load into the class
                parseFirestoreData(data);

                // Get the profile picture
                database.getAllRelatedImagesFromFirestore(this.uid, ImageType.profilePictures).thenAccept(dataList -> {
                    if (dataList == null) {
                        // If the datalist is empty, there is no profile picture
                        this.profileImage = null;

                        // Complete the future
                        future.complete(null);

                        // Data is current
                        dataIsCurrent = true;
                    } else {
                        // Set the UID and profile image
                        this.profileImage = (Bitmap) dataList.get(0).get("image");
                        this.profileImageUID = (String) dataList.get(0).get("UID");

                        // Complete the future
                        future.complete(null);

                        // Data is current
                        dataIsCurrent = true;
                    }
                });

            });
        } else {
            // Data is current, just complete the future
            future.complete(null);
        }

        // Return the future
        return future;
    }

    /*
        Getters
     */

    /**
     * Gets the UID for the user
     *
     * @return The UID of the user
     */
    public String getUID() {
        return uid;
    }


    /**
     * Gets the address of the user
     *
     * @return The address of the user
     */
    public CompletableFuture<String> getAddress() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.address);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the birthday of the user
     *
     * @return The birthday of the user
     */
    public CompletableFuture<Date> getBirthday() {
        // Create a future to return
        CompletableFuture<Date> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.birthday);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the email of the user
     *
     * @return The email of the user
     */
    public CompletableFuture<String> getEmail() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.email);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the homepage of the user
     *
     * @return The homepage of the user
     */
    public CompletableFuture<String> getHomepage() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.homepage);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the name of the user
     *
     * @return The name of the user
     */
    public CompletableFuture<String> getName() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.name);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the user's geolocation preference
     *
     * @return The location preference of the user
     */
    public CompletableFuture<Boolean> getGeolocation() {
        // Create a future to return
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.geolocation);
        });

        // Return the future
        return future;
    }

    /**
     * Sets the user's geolocation preference
     */
    public void setGeolocation(Boolean geolocation) {
        // Update in the class
        this.geolocation = geolocation;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("geolocation", this.geolocation);
        database.storeDataInFirestore(this.uid, data);
    }


    /*
        Setters
     */

    /**
     * Gets the nickname of the user
     *
     * @return The nickname of the user
     */
    public CompletableFuture<String> getNickname() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.nickname);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the image string of the user
     *
     * @return The image string of the user
     */
    public CompletableFuture<String> getProfileImageUID() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.profileImageUID);
        });

        // Return the future
        return future;
    }

    /**
     * Gets the FID of the user
     *
     * @return The FID of the user
     */
    public CompletableFuture<String> getFID() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.fid);
        });

        // Return the future
        return future;
    }

    /**
     * Updates the user's FID
     *
     * @param fid The fid of the installation
     */
    public void setFID(String fid) {
        // Update in the class
        this.fid = fid;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("fid", this.fid);
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Returns whether the current user in an admin
     *
     * @return Whether the user is an admin
     */
    public CompletableFuture<Boolean> isAdmin() {
        // Create a future to return
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.isAdmin);
        });

        // Return the future
        return future;
    }

    /**
     * Makes a user an admin
     */
    public void makeAdmin() {
        // Update in the class
        this.isAdmin = true;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("isAdmin", this.isAdmin);
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the phone number of the user
     *
     * @return The user's phone number
     */
    public CompletableFuture<String> getPhone() {
        // Create a future to return
        CompletableFuture<String> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.phone);
        });

        // Return the future
        return future;
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
        database.storeDataInFirestore(this.uid, data);
    }

    /**
     * Gets the users profile image from the database.
     * The Bitmap profile image is passed out through the future
     */
    public CompletableFuture<Bitmap> getProfileImage() {
        // Create a future to return
        CompletableFuture<Bitmap> future = new CompletableFuture<>();

        // Update data if needed
        CompletableFuture<Void> updateFuture = this.updateUserFromDatabase();

        // Complete the future
        updateFuture.thenAccept(result -> {
            future.complete(this.profileImage);
        });

        // Return the future
        return future;

    }

    /**
     * Sets the user's profile image in the database
     *
     * @param profileImage The image to store as a bitmap
     */
    public void setProfileImage(Bitmap profileImage) {
        // Update in the class
        this.profileImage = profileImage;

        // Update in database
        String imageUID = null;
        if (this.profileImageUID == null) {
            this.profileImageUID = database.storeImageInFirestore(this.uid, null, ImageType.profilePictures, profileImage);
        } else {
            this.profileImageUID = database.storeImageInFirestore(this.uid, this.profileImageUID, ImageType.profilePictures, profileImage);
        }

    }


    /*
        Deleters
     */

    /**
     * Deletes the user's profile image in the database
     */
    public void deleteProfileImage() {
        // Check if there is an image to delete
        if (profileImageUID == null || profileImageUID.isEmpty()) {
            Log.d("User", "No image to delete!");
            return;
        }

        // Set the image to null
        this.profileImage = null;

        // Delete the image from the database
        database.deleteImageFromFirestore(this.uid, this.profileImageUID, ImageType.profilePictures);

        // Set the image UID to null
        this.profileImageUID = null;
    }

    /**
     * Checks a user into the specified event.
     * It will create a new entry if it does not exist already
     *
     * @param eventID The UID of the event
     */
    public void checkIn(String eventID, Context context) {
        // Check to see if the user has checked in already
        database.getDataFromFirestore(this.uid, FirebaseInnerCollection.checkedInEvents, eventID).thenAccept(data -> {
            // If there is no data create a new one
            if (data == null) {
                // Create a map for the new event data
                HashMap<String, Object> eventInfo = new HashMap<>();

                // Add the data to the internal map
                int count = 1;
                eventInfo.put("count", count);
                eventInfo.put("date-time", new Date());

                // If the user has geolocation on, store their current location
                getUserGeolocation(context).thenAccept(location -> {
                    // Add to the map
                    eventInfo.put("location", location);

                    // Store the data into Firestore
                    database.storeDataInFirestore(this.uid, FirebaseInnerCollection.checkedInEvents, eventID, eventInfo);
                });

            } else {
                // Just update the count
                long count = (long) data.get("count");
                data.put("count", count + 1);

                // Update the location as well
                getUserGeolocation(context).thenAccept(location -> {
                    // Add to the map
                    data.put("location", location);

                    // Remove the eventid
                    data.remove("UID");

                    // Store the data into Firestore
                    database.storeDataInFirestore(this.uid, FirebaseInnerCollection.checkedInEvents, eventID, data);
                });
            }

            // Sign the user up for notifications for the event
            FirebaseMessaging.getInstance().subscribeToTopic(eventID);

            // Save the check-in location IF the user has enabled geolocation tracking
            if (this.geolocation) {
                FirebaseAccess eventDB = new FirebaseAccess(FirestoreAccessType.EVENTS);
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("UserCheckIn", "User has location app permissions disabled");
                    return;
                }
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                                HashMap<String, Object> geoLocData = new HashMap<String, Object>();
                                Log.d("UserCheckIn", String.valueOf(point));
                                geoLocData.put("coordinates", point);
                                geoLocData.put("userId", getUID());
                                geoLocData.put("timestamp", new java.sql.Timestamp(System.currentTimeMillis()));
                                eventDB.storeDataInFirestore(eventID, FirebaseInnerCollection.checkInCoords, null, geoLocData);
                            }
                        }
                    });
            }
        });
    }

    /**
     * Signs up a user for the specified event.
     * It will create a new entry if it does not exist already.
     *
     * @param eventID The UID of the event.
     */
    public void signUp(String eventID) {
        FirebaseAccess eventAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Fetch the event details to check the attendee limit and current signed-up attendees
        eventAccess.getDataFromFirestore(eventID).thenAccept(eventData -> {
            if (eventData != null) {
                int attendeeLimit = eventData.containsKey("attendeeLimit") ? ((Number) eventData.get("attendeeLimit")).intValue() : Integer.MAX_VALUE;
                List<String> signedUpAttendees = eventData.containsKey("signedUpAttendees") ? (List<String>) eventData.get("signedUpAttendees") : new ArrayList<>();

                // Check if the user is already signed up for the event
                database.getDataFromFirestore(this.uid, FirebaseInnerCollection.signedUpEvents, eventID).thenAccept(data -> {
                    // If the user is not already signed up and the attendee limit has not been reached
                    if (data == null && signedUpAttendees.size() < attendeeLimit && !signedUpAttendees.contains(uid)) {
                        // Proceed to sign up the user

                        // Create a map for the new event data
                        HashMap<String, Object> eventInfo = new HashMap<>();
                        eventInfo.put("count", 1); // Initial count
                        eventInfo.put("date-time", new Date());

                        // Store the data into Firestore
                        database.storeDataInFirestore(this.uid, FirebaseInnerCollection.signedUpEvents, eventID, eventInfo);

                        // Sign the user up for notifications for the event
                        FirebaseMessaging.getInstance().subscribeToTopic(eventID);

                        Log.d("User", "User signed up successfully for event: " + eventID);
                    } else {
                        // The attendee limit has been reached or user is already signed up
                        if (data != null) {
                            // Just update the count
                            long count = (long) data.get("count");
                            data.put("count", count + 1);

                            // Remove the UID
                            data.remove("UID");

                            // Store the data into Firestore
                            database.storeDataInFirestore(this.uid, FirebaseInnerCollection.signedUpEvents, eventID, data);
                        }
                        Log.d("User", "Cannot sign up for event: " + eventID + ". Attendee limit reached or already signed up.");
                    }
                }).exceptionally(e -> {
                    Log.e("User", "Error checking user sign up status for event: " + eventID, e);
                    return null;
                });
            } else {
                Log.e("User", "Event not found: " + eventID);
            }
        }).exceptionally(e -> {
            Log.e("User", "Error signing up for event: " + eventID, e);
            return null;
        });
    }



    /**
     * Deletes the current user from the database
     *
     * @return A future for waiting for the operation to complete
     */
    public CompletableFuture<Void> deleteUser() {
        // Create the future to return
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Delete their profile picture
        if (profileImageUID != null) {
            this.deleteProfileImage();
        }

        // Then delete the user from the database
        database.deleteDataFromFirestore(this.uid).thenAccept(result -> {
            future.complete(null);
        });

        // Return the future
        return future;
    }

    // Chat GPT: How do I get a user's location in latitude and longitude in Android using Java. I already have the permissions

    /**
     * Gets the user's current geolocation
     *
     * @param context The context this function is called from
     * @return A Geopoint containing the last known location of the user
     */
    private CompletableFuture<GeoPoint> getUserGeolocation(Context context) {
        // Create a future
        CompletableFuture<GeoPoint> locationFuture = new CompletableFuture<>();

        // Check if location is enabled
        if (!User.this.geolocation) {
            Log.d("Geolocation", "User has location disabled");
            locationFuture.complete(null);
            return locationFuture;
        }

        // Set up for getting location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.getApplicationContext());
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        // Check for permissions again
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // This should never run!! Permissions should be enabled already if we make it here
            Log.e("Geolocation", "Location permissions not given!!");
            locationFuture.complete(null);
            return locationFuture;
        }

        // Get the location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        // Create a geopoint from the location
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                        // Complete the future with the Geopoint
                        locationFuture.complete(geoPoint);
                    }
                });

        // Return the future
        return locationFuture;

    }
}