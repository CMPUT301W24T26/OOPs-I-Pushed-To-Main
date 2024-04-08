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
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
                        checkInCountView.setText((String) attendee.get("count"));
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
        attendeeDataList.clear();

        CompletableFuture<Void> future = new CompletableFuture<>();

        db.getAllDocuments().thenAccept(users -> {
            if (users == null) {
                future.complete(null);
                return;
            }

            for (Map<String, Object> user : users) {
//                user.forEach((key, value) -> System.out.println(key + " asdfhjkl:!!! " + value));
                Log.d(TAG, "asdfhjkl " + (String) user.get("UID"));
                Log.d(TAG, "asdfhjkl " + eventId);
                db.getDataFromFirestore((String) user.get("UID"), FirebaseInnerCollection.checkedInEvents, eventId).thenAccept(checkIn -> {
//                    checkIn.forEach((key, value) -> System.out.println(key + " asdfhjkl:??? " + value));
                    if (checkIn != null) {
                        Log.d(TAG, "asdfhjkl");
                        // Create a new attendee from the user's information
                        Map<String, Object> newAttendee = new HashMap<>();
                        newAttendee.put("name", user.get("name"));
                        newAttendee.put("image", user.get("profileImage"));
                        newAttendee.put("count", checkIn.get("count"));

                        attendeeDataList.add(newAttendee);
                    }

                    if (user.equals(users.get(users.size() - 1))) {
                        future.complete(null);
                    }
                });
            }

            future.thenAccept(value -> runOnUiThread(() -> attendeeAdapter.notifyDataSetChanged()));
        });
    }
}
