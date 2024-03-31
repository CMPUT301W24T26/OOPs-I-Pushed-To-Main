package com.oopsipushedtomain;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.encoders.ObjectEncoder;
import com.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Activity for displaying and editing an attendee's profile.
 * Also sets the on click listeners for the buttons on the profile page
 */
public class ProfileActivity extends AppCompatActivity implements EditFieldDialogFragment.EditFieldDialogListener {

    /**
     * Declare the user
     */
    private User user;

    /**
     * Declare a QRCode for scanning
     */
    private QRCode qrCode;


    /**
     * Declare UI elements for labels and values
     */
    private TextView nameValue, nicknameValue, birthdayValue, homepageValue, addressValue, phoneNumberValue, emailValue;
    /**
     * Declare UI elements for buttons
     */
    private Button eventsButton, scanQRCodeButton, adminButton;

    /**
     * Reference to the geo-location toggle
     */
    private Switch toggleGeolocationSwitch;

    /**
     * The reference to the view of the profile image
     */
    private View profileImageView;
    /**
     * The reference to the image drawable
     */
    private Drawable defaultImage;

    /**
     * The UID of the user
     */
    private String userId; // Get from bundle


    /**
     * Activity result launcher for getting the result of the QRCodeScan
     */
    private ActivityResultLauncher<Intent> qrCodeActivityResultLauncher;

    /**
     * Getting the result from the camera
     */
    private final ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("ProfileActivity", "ActivityResult received");
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    ((ImageView)profileImageView).setImageBitmap(photo);
                    // Upload the image to storage
                    // TODO: un-hardcode userID
                    user = new User("USER-9DRH1BAQZQMGZJEZFMGL", new User.DataLoadedListener() {
                        @Override
                        public void onDataLoaded() {
                            user.setProfileImage(photo);
                        }
                    });
                }
            }
    );

    /**
     * Getting the result from the photo gallery for image upload
     */
    private final ActivityResultLauncher<String> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    // Handle the selected image URI
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(result);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    Bitmap picture = BitmapFactory.decodeStream(inputStream);
                    ((ImageView) profileImageView).setImageURI(result);
                    // Upload the image to storage
                    // TODO: un-hardcode userID
                    user = new User("USER-9DRH1BAQZQMGZJEZFMGL", new User.DataLoadedListener() {
                        @Override
                        public void onDataLoaded() {
                            user.setProfileImage(picture);
                        }
                    });
                }
            }
    );

    /**
     * Initializes the activity and sets up the UI elements
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Load the information about the given user
        userId = getIntent().getStringExtra("userId");
        user = new User(userId, new User.DataLoadedListener() {
            @Override
            public void onDataLoaded() {
                // Initialize the UI elements and load attendee data
                initializeViews();
            }
        });

        CustomFirebaseAuth.getInstance().signIn(userId);  // a mock-up sign in feature

        // Initialize UI elements and load attendee data
        initializeViews();

        // Setup listeners for interactive elements
        setupListeners();

        // Set-up ImageView and set on-click listener
        profileImageView = findViewById(R.id.profileImageView);
        defaultImage = ((ImageView) profileImageView).getDrawable();
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable currentImage = ((ImageView) profileImageView).getDrawable();
                if (currentImage.equals(defaultImage)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Update Profile Image");
                    builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // Take Photo
                                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            cameraResultLauncher.launch(cameraIntent);
                                            break;
                                        case 1: // Choose from Gallery
                                            galleryResultLauncher.launch("image/*");
                                            break;
                                    }
                                }
                            });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Update Profile Image");
                    builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Delete Photo"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // Take Photo
                                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            cameraResultLauncher.launch(cameraIntent);
                                            break;
                                        case 1: // Choose from Gallery
                                            galleryResultLauncher.launch("image/*");
                                            break;
                                        case 2: // Delete Photo
                                            if (user != null) {
                                                user.deleteProfileImage();
                                                ((ImageView) profileImageView).setImageDrawable(defaultImage);
                                            }
                                            break;
                                    }
                                }
                            });
                    builder.show();
                }
            }
        });

        // Qr code activity launcher
        // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()
        // Register the activity result
        qrCodeActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    // Get the data
                    Intent data = o.getData();

                    // If the data is not null, load the QR code
                    if (data != null) {
                        String qrCodeString = data.getStringExtra("result");

                        // Check the user into the event
                        user.checkIn(qrCodeString);

                        // Show the scanned data
                        Toast.makeText(getApplicationContext(), "Checked into event: " + qrCodeString, Toast.LENGTH_LONG).show();
                        Log.d("QR Code", "Checked into event: " + qrCodeString);

                    } else {
                        Toast.makeText(getApplicationContext(), "Data Error", Toast.LENGTH_LONG).show();
                        Log.d("QR Code", "QR Code Not Scanned");
                    }
                }
            }
        });
    }

    /**
     * Updates the user elements on the UI
     */
    private void updateUIElements() {

        // Get the data from user
        String name = user.getName();
        String nickname = user.getNickname();
        String homepage = user.getHomepage();
        String address = user.getAddress();
        String phone = user.getPhone();
        String email = user.getEmail();
        Date birthday = user.getBirthday();

        // Update the fields
        if (name != null) {
            nameValue.setText(name);
        }

        if (nickname != null) {
            nicknameValue.setText(nickname);
        }

        if (birthday != null) {
            // Format the date
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            birthdayValue.setText(formatter.format(birthday));
        }

        if (homepage != null) {
            homepageValue.setText(homepage);
        }

        if (address != null) {
            addressValue.setText(address);
        }

        if (phone != null) {
            phoneNumberValue.setText(phone);
        }

        if (email != null) {
            emailValue.setText(email);
        }
    }

    /**
     * Handles the positive click action from the edit field dialog.
     * Updates the corresponding profile field with the new value entered by the user.
     *
     * @param dialog The dialog that was clicked on
     * @param fieldName The field name that was clicked on
     * @param fieldValue The value of the dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String fieldName, String fieldValue) {

        // Update the corresponding field in your UI based on fieldName
        Map<String, Object> update = new HashMap<>();
        switch (fieldName) {
            case "Name":
                nameValue.setText(fieldValue);
                user.setName(fieldValue);
                break;
            case "Nickname":
                nicknameValue.setText(fieldValue);
                user.setNickname(fieldValue);
                break;
            case "Birthday":
                birthdayValue.setText(fieldValue);

                // Format the given date, ChatGPT: How do i format a string into date
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date birthday = formatter.parse(fieldValue);
                    user.setBirthday(birthday);
                } catch (ParseException e) {
                    Log.d("ProfileActivity", "Date formatting failed");
                }
                break;
            case "Homepage":
                homepageValue.setText(fieldValue); // Corrected to update homepageTextView
                user.setHomepage(fieldValue);
                break;
            case "Address":
                addressValue.setText(fieldValue); // Corrected to update addressTextView
                user.setAddress(fieldValue);
                break;
            case "Phone Number":
                phoneNumberValue.setText(fieldValue); // Corrected to update phoneNumberTextView
                user.setPhone(fieldValue);
                break;
            case "Email":
                emailValue.setText(fieldValue); // Corrected to update emailTextView
                user.setEmail(fieldValue);
                break;
        }
    }

    /**
     * Handles the positive click action from the edit field dialog.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    /**
     * Shows the edit field dialog for a field on the page using its current value
     *
     * @param fieldName The field we are editing
     * @param fieldValue The value of the field
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

        // Initialize values (TextViews for field values)
        nameValue = findViewById(R.id.nameTextView);
        nicknameValue = findViewById(R.id.nicknameTextView);
        birthdayValue = findViewById(R.id.birthdayValueTextView);
        homepageValue = findViewById(R.id.homepageValueTextView);
        addressValue = findViewById(R.id.addressValueTextView);
        phoneNumberValue = findViewById(R.id.phoneNumberValueTextView);
        emailValue = findViewById(R.id.emailValueTextView);

        // Initialize buttons
        eventsButton = findViewById(R.id.eventsButton);
        scanQRCodeButton = findViewById(R.id.scanQRCodeButton);
        adminButton = findViewById(R.id.adminButton);

        // Initialize switch
        toggleGeolocationSwitch = findViewById(R.id.toggleGeolocationSwitch);

        // Load user data into views
        updateUIElements();
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
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });
        scanQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a new document to the database
                database.storeDataInFirestore("matteotest", new HashMap<>());

                HashMap<String, Object> inData = new HashMap<>();
                inData.put("test", "this is a test");
                database.storeDataInFirestore("matteotest", FirebaseInnerCollection.announcements, "TEST", inData);

                // Get the document
                database.getDataFromFirestore("matteotest").thenAccept(data -> {
                    Log.d("Testing", "Data: " + data.get("UID"));
                });

                // Get the inner document
                database.getDataFromFirestore("matteotest", FirebaseInnerCollection.announcements, "TEST").thenAccept(data -> {
                    Log.d("Testing", "Data: " + data.get("test"));
                });

                // Delete the documents
                database.deleteDataFromFirestore("matteotest").thenAccept(vdd -> {
                    Log.d("Testing", "Delete Complete");
                });



                // Switch to the scanning activity and scan
//                Intent intent = new Intent(getApplicationContext(), QRScanner.class);

                // Start the activity
//                qrCodeActivityResultLauncher.launch(intent);

                // This is asynchronous. DO NOT PUT CODE HERE

            }

        });
    }

    // Database test
    FirebaseAccess database = new FirebaseAccess(FirestoreAccessType.EVENTS);

}
