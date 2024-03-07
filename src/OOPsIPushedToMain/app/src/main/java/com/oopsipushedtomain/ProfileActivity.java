package com.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.oopsipushedtomain.Announcements.AnnouncementListActivity;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying and editing an attendee's profile.
 * This class allows for interaction with Firebase Firestore to retrieve and update user data.
 */
public class ProfileActivity extends AppCompatActivity implements EditFieldDialogFragment.EditFieldDialogListener {

    // Declare UI elements for labels and values
    private TextView nameLabel, nicknameLabel, birthdayLabel, homepageLabel, addressLabel, phoneNumberLabel, emailLabel;
    private TextView nameValue, nicknameValue, birthdayValue, homepageValue, addressValue, phoneNumberValue, emailValue;
    private Button notificationsButton, eventsButton, announcementsButton, scanQRCodeButton;
    private Switch toggleGeolocationSwitch;
    private FirebaseFirestore db;
    private String userId = "USER-0000000000"; // Get from bundle

    /**
     * Initializes the activity, sets up the UI elements, and prepares Firestore interaction
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements and load attendee data
        initializeViews();

        // Setup listeners for interactive elements
        setupListeners();
    }

    /**
     * Gets user data based on user ID and loads data
     */
    private void loadUserDataFromFirestore() {
        //db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Extract fields and update UI
                        Timestamp birthdayTimestamp = document.getTimestamp("birthday");
                        Date birthdayDate = birthdayTimestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String birthday = sdf.format(birthdayDate);
                        birthdayValue.setText(birthday);

                        String name = document.getString("name");
                        String nickname = document.getString("nickname");
                        String homepage = document.getString("homepage");
                        String address = document.getString("address");
                        String phone = document.getLong("phone").toString();
                        String email = document.getString("email");

                        // Update UI elements
                        nameValue.setText(name);
                        nicknameValue.setText(nickname);
                        birthdayValue.setText(birthday);
                        homepageValue.setText(homepage);
                        addressValue.setText(address);
                        phoneNumberValue.setText(phone);
                        emailValue.setText(email);
                    } else {
                        Log.d("Document", "No such document");
                    }
                } else {
                    Log.d("Document", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Handles the positive click action from the edit field dialog.
     * Updates the corresponding profile field with the new value entered by the user.
     * @param dialog
     * @param fieldName
     * @param fieldValue
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String fieldName, String fieldValue) {

        // Update the corresponding field in your UI based on fieldName
        Map<String, Object> update = new HashMap<>();
        switch (fieldName) {
            case "Name":
                nameValue.setText(fieldValue);
                update.put("name", fieldValue);
                break;
            case "Nickname":
                nicknameValue.setText(fieldValue);
                update.put("nickname", fieldValue);
                break;
            case "Birthday":
                // TODO: Implement format for birthday field
                birthdayValue.setText(fieldValue); // Corrected to update birthdayTextView
                update.put("birthday", fieldValue);
                break;
            case "Homepage":
                homepageValue.setText(fieldValue); // Corrected to update homepageTextView
                update.put("homepage", fieldValue);
                break;
            case "Address":
                addressValue.setText(fieldValue); // Corrected to update addressTextView
                update.put("address", fieldValue);
                break;
            case "Phone Number":
                // TODO: Implement format for phone number field
                phoneNumberValue.setText(fieldValue); // Corrected to update phoneNumberTextView
                update.put("phone", fieldValue);
                break;
            case "Email":
                emailValue.setText(fieldValue); // Corrected to update emailTextView
                update.put("email", fieldValue);
                break;
        }

        // Update Firestore
        if (!update.isEmpty()) {
            db.collection("users").document(userId)
                    .update(update)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating document", e));
        }
    }

    /**
     * Handles the positive click action from the edit field dialog.
     * @param dialog
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    /**
     * Shows the edit field dialog for a field on the page using its current value
     * @param fieldName
     * @param fieldValue
     */
    public void showEditFieldDialog(String fieldName, String fieldValue) {
        DialogFragment dialog = new EditFieldDialogFragment();
        Bundle args = new Bundle();
        args.putString("fieldName", fieldName);
        args.putString("fieldValue", fieldValue);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "EditFieldDialogFragment");
    }

    /**
     * Initialize views to connect to layout file
     */
    private void initializeViews() {
        // Initialize labels (TextViews for field names)
        birthdayLabel = findViewById(R.id.birthdayLabelTextView);
        homepageLabel = findViewById(R.id.homepageLabelTextView);
        addressLabel = findViewById(R.id.addressLabelTextView);
        phoneNumberLabel = findViewById(R.id.phoneNumberLabelTextView);
        emailLabel = findViewById(R.id.emailLabelTextView);

        // Initialize values (TextViews for field values)
        nameValue = findViewById(R.id.nameTextView);
        nicknameValue = findViewById(R.id.nicknameTextView);
        birthdayValue = findViewById(R.id.birthdayValueTextView);
        homepageValue = findViewById(R.id.homepageValueTextView);
        addressValue = findViewById(R.id.addressValueTextView);
        phoneNumberValue = findViewById(R.id.phoneNumberValueTextView);
        emailValue = findViewById(R.id.emailValueTextView);

        // Initialize buttons
        notificationsButton = findViewById(R.id.notificationsButton);
        eventsButton = findViewById(R.id.eventsButton);
        announcementsButton = findViewById(R.id.announcementsButton);
        scanQRCodeButton = findViewById(R.id.scanQRCodeButton);
        announcementsButton = findViewById(R.id.announcementsButton);

        // Initialize switch
        toggleGeolocationSwitch = findViewById(R.id.toggleGeolocationSwitch);

        // Load user data into views
        loadUserDataFromFirestore();
    }

    /**
     * Set listeners for clickable fields on the page
     */
    private void setupListeners() {
        // Set click listeners for each editable field
        nameValue.setOnClickListener(v -> showEditFieldDialog("Name", nameValue.getText().toString()));
        nicknameValue.setOnClickListener(v -> showEditFieldDialog("Nickname", nicknameValue.getText().toString()));
        birthdayValue.setOnClickListener(v -> showEditFieldDialog("Birthday", birthdayValue.getText().toString()));
        homepageValue.setOnClickListener(v -> showEditFieldDialog("Homepage", homepageValue.getText().toString()));
        addressValue.setOnClickListener(v -> showEditFieldDialog("Address", addressValue.getText().toString()));
        phoneNumberValue.setOnClickListener(v -> showEditFieldDialog("Phone Number", phoneNumberValue.getText().toString()));
        emailValue.setOnClickListener(v -> showEditFieldDialog("Email", emailValue.getText().toString()));
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        });
        announcementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, AnnouncementListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }
}
