package com.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for viewing and limiting the number of attendees for an event.
 * <p>
 * This activity displays a list of signed-up attendees for a specific event and allows the organizer
 * to set a limit on the number of attendees. It provides editable fields for the event's attendee limit
 * and includes a ListView to display the names or IDs of the attendees who have signed up.
 * </p>
 * <p>
 * The activity fetches the list of signed-up attendees from Firestore using the event's unique identifier (eventId)
 * and updates the attendee limit in Firestore upon confirmation.
 */

public class ViewLimitAttendeesActivity extends AppCompatActivity {

    private ListView signedUpAttendeesListView;
    private EditText limitEditText;
    private Button setLimitButton;
    private ArrayList<String> signedUpAttendees = new ArrayList<>();
    private ArrayAdapter<String> signedUpAttendeesAdapter;
    private String eventId;
    private FirebaseAccess firebaseAccess;

    /**
     * Initializes the activity, its views, and fetches the list of signed-up attendees.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState.
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_limit_attendees);

        signedUpAttendeesListView = findViewById(R.id.signedUpAttendeesListView);
        limitEditText = findViewById(R.id.limitEditText);
        setLimitButton = findViewById(R.id.setLimitButton);

        eventId = getIntent().getStringExtra("eventId");
        firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);

        signedUpAttendeesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, signedUpAttendees);
        signedUpAttendeesListView.setAdapter(signedUpAttendeesAdapter);

        fetchSignedUpAttendees();

        setLimitButton.setOnClickListener(v -> setAttendeeLimit());
    }

    /**
     * Fetches the list of signed-up attendees for the event from Firestore and updates the ListView.
     * This method uses the {@link FirebaseAccess} class to asynchronously fetch event data based on the eventId.
     */
    private void fetchSignedUpAttendees() {
        firebaseAccess.getDataFromFirestore(eventId).thenAccept(eventData -> {
            if (eventData != null && eventData.containsKey("signedUpAttendees")) {
                signedUpAttendees.clear();
                signedUpAttendees.addAll((List<String>) eventData.get("signedUpAttendees"));
                runOnUiThread(() -> signedUpAttendeesAdapter.notifyDataSetChanged());
            }
        }).exceptionally(e -> {
            Log.e("ViewLimitAttendeesActivity", "Error fetching signed up attendees", e);
            return null;
        });
    }

    /**
     * Sets the attendee limit for the event in Firestore.
     * This method reads the attendee limit from the EditText field and updates the event's attendee limit in Firestore.
     */
    private void setAttendeeLimit() {
        String limitStr = limitEditText.getText().toString();
        if (!limitStr.isEmpty()) {
            int limit = Integer.parseInt(limitStr);
            Map<String, Object> update = new HashMap<>();
            update.put("attendeeLimit", limit);
            firebaseAccess.storeDataInFirestore(eventId, update);
        }
    }
}
