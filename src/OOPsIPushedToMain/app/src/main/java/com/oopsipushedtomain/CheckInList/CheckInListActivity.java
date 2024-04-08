package com.oopsipushedtomain.CheckInList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.User;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.EventListActivity;
import com.oopsipushedtomain.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class CheckInListActivity extends AppCompatActivity {

    /**
     * List to store attendees
     */
    private ArrayList<Map<String, Object>> attendeeDataList;

    /**
     * Custom ArrayAdapter to show attendees in the ListView
     */
    private ArrayAdapter<Map<String, Object>> attendeeAdapter;

    /**
     * Database access object
     */
    private FirebaseAccess db;

    /**
     * The UID of the event to check for
     */
    private String eventId;

    private String userId;
    private final String TAG = "CheckInList";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin_list); // Layout for activity

        attendeeDataList = new ArrayList<>();
        attendeeAdapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.activity_checkin_list, attendeeDataList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_checked_in_attendee, parent, false);
                }

                Map<String, Object> attendee = getItem(position);
                if (attendee != null) {
                    ImageView attendeeImageView = view.findViewById(R.id.userImageView);
                    TextView attendeeNameView = view.findViewById(R.id.nameTextView);
                    TextView checkInCountView = view.findViewById(R.id.checkInCount);
                    runOnUiThread(() -> {
                        attendeeNameView.setText((String) attendee.get("name"));
                        attendeeImageView.setImageBitmap((Bitmap) attendee.get("image"));
                        checkInCountView.setText(attendee.get("count").toString());
                    });
                }

                return view;
            }
        };

        ListView attendeeListView = findViewById(R.id.attendee_list);
        if (attendeeAdapter != null) {
            attendeeListView.setAdapter(attendeeAdapter);
        }

        // Get intent data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        if (eventId != null)
            Log.d(TAG, eventId);

        // Initialize Firestore instance and get the event's announcements
        db = new FirebaseAccess(FirestoreAccessType.USERS);
        if (eventId != null)
            getCheckedInAttendees();
    }

    /**
     *
     */
    public void getCheckedInAttendees() {
        // Clear the list
        attendeeDataList.clear();

        // Create a callable
        Callable<ArrayList<Void>> getDataTask = () -> {
            // Get the list of checked in events
            ArrayList<Map<String, Object>> users =  db.getAllDocuments().get();

            // Check if this is null
            if (users == null){
                return null;
            }

            // Iterate through the users
            String userId;
            for (Map<String, Object> user : users){
                // Create a new event
                userId = (String) user.get("UID");

                // Get the event details from the database
                Map<String, Object> checkInData = db.getDataFromFirestore(userId, FirebaseInnerCollection.checkedInEvents, eventId).get();
                if (checkInData != null) {
                    // Set the params
                    if (checkInData.get("UID").equals(eventId)) {
                        Map<String, Object> newAttendee = new HashMap<>();
                        newAttendee.put("name", user.get("name"));
                        newAttendee.put("image", user.get("profileImage"));
                        newAttendee.put("count", checkInData.get("count"));

                        attendeeDataList.add(newAttendee);
                    }
                }
            }

            // All done, notify that dataset has changed
            runOnUiThread(() -> {
                attendeeAdapter.notifyDataSetChanged();
            });

            return null;

        };

        // Run the task
        db.callableToCompletableFuture(getDataTask);

        // TODO OLD
//        CompletableFuture<Void> future = new CompletableFuture<>();
//
//        db.getAllDocuments().thenAccept(users -> {
//            if (users == null) {
//                future.complete(null);
//                return;
//            }
//            for (Map<String, Object> user : users) {
////                Log.d(TAG, "asdfhjkl " + user.get("UID"));
////                Log.d(TAG, "asdfhjkl " + eventId);
//                userId = (String) user.get("UID");
//                db.getDataFromFirestore(userId, FirebaseInnerCollection.checkedInEvents, eventId).thenAccept(checkIn -> {
////                    checkIn.forEach((key, value) -> System.out.println(key + " asdfhjkl:??? " + value));
//                    if (checkIn != null) {
//                        Log.d(TAG, "asdfhjkl");
//                        // Create a new attendee from the user's information
//                        Map<String, Object> newAttendee = new HashMap<>();
//                        newAttendee.put("name", user.get("name"));
//                        newAttendee.put("image", user.get("profileImage"));
//                        newAttendee.put("count", checkIn.get("count"));
//
//                        attendeeDataList.add(newAttendee);
//                    }
//                    if (user.equals(users.get(users.size() - 1))) {
//                        future.complete(null);
//                    }
//                });
//            }
//            future.thenAccept(value -> runOnUiThread(() -> attendeeAdapter.notifyDataSetChanged()));
//        });
    }
}
