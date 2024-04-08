package com.oopsipushedtomain.CheckInList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * This activity obtains and displays a list of users that are checked into a given event. The
 * eventId is passed to this activity through an Intent, which is then used to compare against
 * the checked-in events of every user in the database.
 *
 * @author Aidan Gironella
 * @see com.oopsipushedtomain.EventDetailsActivity
 */
public class CheckInListActivity extends AppCompatActivity {

    /**
     * List to store attendees that have checked into the event
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

    /**
     * Used to store the user ID of every user that we check
     */
    private String userId;

    /**
     * Handler to periodically call getCheckedInAttendees()
     */
    private final Handler mHandler = new Handler();

    /**
     * Runnable task to be periodically called
     */
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Running task");
            if (eventId != null)
                getCheckedInAttendees();
            mHandler.postDelayed(this, DELAY_MS);
        }
    };

    /**
     * Delay between getCheckedInAttendees() calls
     */
    private static final long DELAY_MS = 10000;

    /**
     * Tag for logging messages
     */
    private static final String TAG = "CheckInList";


    /**
     * Creates and instantiates the activity
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
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

                // Update UI elements based on attendee information
                Map<String, Object> attendee = getItem(position);
                if (attendee != null) {
                    TextView attendeeNameView = view.findViewById(R.id.nameTextView);
                    TextView checkInCountView = view.findViewById(R.id.checkInCount);
                    runOnUiThread(() -> {
                        attendeeNameView.setText((String) attendee.get("name"));
                        checkInCountView.setText(Objects.requireNonNull(attendee.get("count")).toString());
                    });
                }

                return view;
            }
        };

        // Initialize the list and adapter
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

        mHandler.postDelayed(mRunnable, DELAY_MS);
    }

    /**
     * Populates the attendeeDataList with all users who are checked into the given event
     * First queries the database to get an ArrayList of all users, then checks the
     * FirebaseInnerCollection.checkedInEvents of every user for the given eventId
     */
    public void getCheckedInAttendees() {
        // Clear the list
        attendeeDataList.clear();

        // Create a callable
        Callable<ArrayList<Void>> getDataTask = () -> {
            // Get the list of all users in the database
            ArrayList<Map<String, Object>> users =  db.getAllDocuments().get();

            // Check if this is null
            if (users == null){
                return null;
            }

            // Iterate through the users
            for (Map<String, Object> user : users){
                // Get the user ID
                userId = (String) user.get("UID");

                // Get the check-in details from the database
                Map<String, Object> checkInData = db.getDataFromFirestore(userId, FirebaseInnerCollection.checkedInEvents, eventId).get();
                if (checkInData != null) {
                    // Data exists = user is checked into this event, set the params
                    if (Objects.equals(checkInData.get("UID"), eventId)) {
                        Map<String, Object> newAttendee = new HashMap<>();
                        newAttendee.put("name", user.get("name"));
                        newAttendee.put("count", checkInData.get("count"));

                        attendeeDataList.add(newAttendee);
                    }
                }
            }

            // All done, notify that dataset has changed
            runOnUiThread(() -> attendeeAdapter.notifyDataSetChanged());
            return null;
        };

        // Run the task
        db.callableToCompletableFuture(getDataTask);
    }

    /**
     * Kill the runnable task that runs every DELAY_MS milliseconds
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroying activity");
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }
}
