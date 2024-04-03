package com.oopsipushedtomain;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * Utility class for fetching events from the Firestore database.
 * This class provides static methods to interact with the Firestore database
 * and retrieve event data asynchronously.
 */
public class FirestoreEventUtils {

    /**
     * Fetches all events from the Firestore database and provides them through a callback.
     * This method asynchronously retrieves a list of all event documents from the "events" collection
     * in Firestore. Each document is converted into an {@link Event} object. The list of events
     * is then passed to the provided callback function.
     *
     * @param callback A {@link Consumer} callback that handles the list of {@link Event} objects fetched.
     *                 The callback is called with the list of events upon successful retrieval.
     *                 If an error occurs during the fetch operation, the error is logged, and the callback
     *                 is not called.
     */
    public static void getAllEvents(Consumer<List<Event>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            List<Event> events = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    event.setEventId(document.getId());
                    events.add(event);
                }
            } else {
                Log.e("FirestoreEventUtils", "Error getting events", task.getException());
            }
            callback.accept(events);
        });
    }
}

