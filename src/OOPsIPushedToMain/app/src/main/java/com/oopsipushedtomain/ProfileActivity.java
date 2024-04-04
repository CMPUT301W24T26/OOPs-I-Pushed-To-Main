package com.oopsipushedtomain;

import android.Manifest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

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

/**
 * Activity for displaying and editing an attendee's profile.
 * Also sets the on click listeners for the buttons on the profile page
 */
public class ProfileActivity extends AppCompatActivity implements EditFieldDialogFragment.EditFieldDialogListener {

    /**
     * A unique request code for getting camera permissions
     */
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100; // A unique request code
    // Database test
    FirebaseAccess database = new FirebaseAccess(FirestoreAccessType.EVENTS);
    boolean cameraEnabled;
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
    private SwitchCompat toggleGeolocationSwitch;
    /**
     * The reference to the view of the profile image
     */
    private View profileImageView;
    /**
     * Get the image from the camera
     */
    private final ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("ProfileActivity", "ActivityResult received");
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    ((ImageView) profileImageView).setImageBitmap(photo);
                    // Upload the image to storage
                    // TODO: un-hardcode userID
                    User.createNewObject("USER-9DRH1BAQZQMGZJEZFMGL").thenAccept(newUser -> {
                        user = newUser;
                        user.setProfileImage(photo);
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
                    User.createNewObject("USER-9DRH1BAQZQMGZJEZFMGL").thenAccept(newUser -> {
                        user = newUser;
                        user.setProfileImage(picture);
                    });
                }
            }
    );
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
     * Performs the correct action depending on the type of QR code scanned.
     */
    private ActivityResultLauncher<Intent> qrCodeActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()
        // Register the activity result
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
        userId = getIntent().getStringExtra("userID");

        // Create a new User object from the userID
        User.createNewObject(userId).thenAccept(newUser -> {
            runOnUiThread(() -> {
                // User is created, store in the class
                user = newUser;

                // Update the views on the new data
                updateUIElements();

                // TODO: Implement a proper sign in feature
                CustomFirebaseAuth.getInstance().signIn(userId);  // a mock-up sign in feature


            });
        });


        /*
            Find views
         */
        // TextViews
        nameValue = findViewById(R.id.nameTextView);
        nicknameValue = findViewById(R.id.nicknameTextView);
        birthdayValue = findViewById(R.id.birthdayValueTextView);
        homepageValue = findViewById(R.id.homepageValueTextView);
        addressValue = findViewById(R.id.addressValueTextView);
        phoneNumberValue = findViewById(R.id.phoneNumberValueTextView);
        emailValue = findViewById(R.id.emailValueTextView);

        // Buttons
        eventsButton = findViewById(R.id.eventsButton);
        scanQRCodeButton = findViewById(R.id.scanQRCodeButton);
        adminButton = findViewById(R.id.adminButton);

        // Switch
        toggleGeolocationSwitch = findViewById(R.id.toggleGeolocationSwitch);

        // Profile pictures
        profileImageView = findViewById(R.id.profileImageView);
        // Get the default profile picture
        defaultImage = ((ImageView) profileImageView).getDrawable();


        /*
            Set click listeners
         */
        // Set up the click listeners for the TextViews
        nameValue.setOnClickListener(v -> showEditFieldDialog("Name", nameValue.getText().toString()));
        nicknameValue.setOnClickListener(v -> showEditFieldDialog("Nickname", nicknameValue.getText().toString()));
        birthdayValue.setOnClickListener(v -> showEditFieldDialog("Birthday", birthdayValue.getText().toString()));
        homepageValue.setOnClickListener(v -> showEditFieldDialog("Homepage", homepageValue.getText().toString()));
        addressValue.setOnClickListener(v -> showEditFieldDialog("Address", addressValue.getText().toString()));
        phoneNumberValue.setOnClickListener(v -> showEditFieldDialog("Phone Number", phoneNumberValue.getText().toString()));
        emailValue.setOnClickListener(v -> showEditFieldDialog("Email", emailValue.getText().toString()));

        // Set up click listeners for the buttons
        eventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
            intent.putExtra("userId", user.getUID()); // Assuming userId is the ID of the current user
            startActivity(intent);
        });
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });
        scanQRCodeButton.setOnClickListener(v -> {
            // Switch to the QR code scanner
            Intent intent = new Intent(getApplicationContext(), QRScanner.class);
            qrCodeActivityResultLauncher.launch(intent);
        });


        // Set up click listener for the profile image
        profileImageView.setOnClickListener(v -> handleProfileImageClick());
    }

    /**
     * Updates the user elements on the UI
     */
    private void updateUIElements() {

        // Update the name
        user.getName().thenAccept(name -> {
            runOnUiThread(() -> {
                // Update the fields
                if (name != null) {
                    nameValue.setText(name);
                }
            });
        });

        // Update the nickname
        user.getNickname().thenAccept(nickname -> {
            runOnUiThread(() -> {
                // Update the fields
                if (nickname != null) {
                    nicknameValue.setText(nickname);
                }
            });
        });

        // Update the birthday
        user.getBirthday().thenAccept(birthday -> {
            runOnUiThread(() -> {
                // Update the fields
                if (birthday != null) {
                    // Format the date
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    birthdayValue.setText(formatter.format(birthday));
                }
            });
        });

        // Update the homepage
        user.getHomepage().thenAccept(homepage -> {
            runOnUiThread(() -> {
                // Update the fields
                if (homepage != null) {
                    homepageValue.setText(homepage);
                }
            });
        });

        // Update the address
        user.getNickname().thenAccept(address -> {
            runOnUiThread(() -> {
                // Update the fields
                if (address != null) {
                    addressValue.setText(address);
                }
            });
        });

        // Update the phone
        user.getNickname().thenAccept(phone -> {
            runOnUiThread(() -> {
                // Update the fields
                if (phone != null) {
                    phoneNumberValue.setText(phone);
                }
            });
        });

        // Update the email
        user.getNickname().thenAccept(email -> {
            runOnUiThread(() -> {
                // Update the fields
                if (email != null) {
                    emailValue.setText(email);
                }
            });
        });
    }


    public void handleProfileImageClick() {
        // Get the current profile image
        Drawable currentImage = ((ImageView) profileImageView).getDrawable();

        // Build a new alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Update Profile Image");

        // If the current image is the default image, don't show the delete option
        if (currentImage.equals(defaultImage)) {
            // Set the items in the dialog
            builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"},
                    // Set the listener for the selection on the dialog
                    (dialog, which) -> {
                        switch (which) {
                            // Take a photo using the camera
                            case 0: // Take Photo
                                // Check for camera permissions
                                if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    // Permission is not granted, request permissions
                                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                                } else {
                                    // Camera permissions are granted
                                    cameraEnabled = true;
                                }

                                // Perform the proper action
                                if (cameraEnabled){
                                    // Open the camera
                                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    cameraResultLauncher.launch(cameraIntent);
                                    Log.d("Camera", "Picture taken!");
                                    break;
                                } else {
                                    // Show a message saying camera permission is required
                                    Log.d("Camera", "No permissions!");
                                    Toast.makeText(ProfileActivity.this, "Camera Permissions Required!!", Toast.LENGTH_LONG).show();
                                    return;
                                }


                            case 1: // Choose from Gallery
                                galleryResultLauncher.launch("image/*");
                                Log.d("Gallery", "Image selected from the gallery");
                                break;
                        }
                    });
            builder.show();
        } else {
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

    /**
     * Handles when permissions are requested
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(
     *android.app.Activity, String[], int)}
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, you can use the camera
                    cameraEnabled = true;
                } else {
                    // Permission denied, disable camera
                    cameraEnabled = false;
                    // Show a message saying camera permission is required
                    Toast.makeText(ProfileActivity.this, "Camera Permissions Required!!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    /**
     * Handles the positive click action from the edit field dialog.
     * Updates the corresponding profile field with the new value entered by the user.
     *
     * @param dialog     The dialog that was clicked on
     * @param fieldName  The field name that was clicked on
     * @param fieldValue The value of the dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String fieldName, String
            fieldValue) {

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

}