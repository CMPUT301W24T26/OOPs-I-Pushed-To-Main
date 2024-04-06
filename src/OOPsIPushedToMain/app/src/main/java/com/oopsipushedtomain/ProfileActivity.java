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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.google.firebase.Firebase;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;
import com.oopsipushedtomain.DialogInputListeners.CustomDatePickerDialog;
import com.oopsipushedtomain.DialogInputListeners.InputTextDialog;
import com.oopsipushedtomain.DialogInputListeners.PhonePickerDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class ProfileActivity extends AppCompatActivity {

    /**
     * A unique request code for getting camera permissions
     */
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100; // A unique request code

    /**
     * A unique request code for getting geolocation permissions
     */
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 110; // A unique request code

    /**
     * Toggle for camera permissions
     */
    boolean cameraEnabled;

    /**
     * Toggle for location permissions
     */
    boolean locationEnabled;

    /**
     * Whether this is the first time the geolocation permission is checked
     */
    boolean initialRun = true;

    /**
     * The signed in user
     */
    private User user;

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
                // If the returned result is good
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get the bitmap
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    // Set the image in the view
                    ((ImageView) profileImageView).setImageBitmap(photo);
                    // Update the user profile
                    if (user != null) {
                        // Set the new profile image
                        user.setProfileImage(photo);
                    } else {
                        Log.d("ProfileActivity", "User object is null");
                    }
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
                    // Get the bitmap image
                    InputStream finalInputStream = inputStream;
                    Bitmap picture = BitmapFactory.decodeStream(finalInputStream);

                    // Set in the view
                    ((ImageView) profileImageView).setImageURI(result);

                    // Update the user
                    if (user != null) {
                        // Set the new profile image
                        user.setProfileImage(picture);
                    } else {
                        Log.d("ProfileActivity", "User object is null");
                    }

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

                // If the data is not null, load the scanned QR code
                if (data != null) {
                    String qrCodeData = data.getStringExtra("result");

                    // Get the QR code data from the database
                    FirebaseAccess eventsDatabase = new FirebaseAccess(FirestoreAccessType.EVENTS);
                    // As long as the image type is an QR code it will access the correct database
                    eventsDatabase.getImageFromFirestore(qrCodeData, ImageType.eventQRCodes).thenAccept(imageData -> {
                        // If no data is found cancel
                        if (imageData == null) {
                            runOnUiThread(() -> Log.d("QRScanner", "No QR image found"));

                            return;
                        }

                        // Perform the correct action depending on the type of QR code
                        String qrType = (String) imageData.get("type");
                        if (qrType == null) {
                            // There was a problem
                            runOnUiThread(() -> Log.d("QR Scanner", "Error getting type"));
                            return;
                        }

                        // Get the eventID, for admin will be null
                        String eventID = (String) imageData.get("origin");

                        switch (qrType) {
                            case "eventQRCodes":
                                // Get the event name
                                eventsDatabase.getDataFromFirestore(eventID).thenAccept(event -> {
                                    runOnUiThread(() -> {
                                        // Check the event exists
                                        if (event == null) {
                                            runOnUiThread(() -> Log.d("QRScanner", "Event does not exist!"));
                                            return;
                                        }
                                        // Get the title of the event
                                        String eventTitle = (String) event.get("title");

                                        // Check the user into the event
                                        runOnUiThread(() -> {
                                            user.checkIn(eventID, ProfileActivity.this);
                                        });


                                        // Show a confirmation message
                                        Toast.makeText(getApplicationContext(), "Checked into event: " + eventTitle, Toast.LENGTH_LONG).show();
                                    });
                                });
                                break;
                            case "promoQRCodes":
                                // Open the event details page
                                runOnUiThread(() -> {
                                    // Get the id of the loaded user
                                    Intent intent = new Intent(ProfileActivity.this, EventDetailsActivity.class);
                                    intent.putExtra("userId", user.getUID());
                                    intent.putExtra("selectedEventId", eventID);
                                    startActivity(intent);
                                });
                                break;
                            case "admin":
                                // Make the user an admin
                                // TODO: Update user to support account types
                                break;

                        }
                    });
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
                Log.d("USER", user.getUID());

                // Update the views on the new data
                updateUIElements();

                // TODO: Implement a proper sign in feature
                CustomFirebaseAuth.getInstance().signIn(userId);  // a mock-up sign in feature

                // Set the toggle switch based of the loaded value
                user.getGeolocation().thenAccept(value -> {
                    runOnUiThread(() -> {
                        toggleGeolocationSwitch.setChecked(value);

                        // Request location permissions
                        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            // At least one of the permissions are not granted, request them
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

                        }
                        initialRun = false;
                    });
                });


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
        // Name
        nameValue.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    nameValue.setText((String) input);
                    user.setName((String) input);
                }
            });

            // Get the name of the user
            user.getName().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Name", data);
                });
            });

        });

        // Nickname
        nicknameValue.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    nicknameValue.setText((String) input);
                    user.setNickname((String) input);
                }
            });

            // Get the nickname of the user
            user.getNickname().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Nickname", data);
                });
            });

        });

        //Birthday
        birthdayValue.setOnClickListener(v -> {
            // This should be a date picker
            CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(this, input -> {
                // Convert the input to a date
                Date inputDate = (Date) input;

                // Format the given date, ChatGPT: How do i format a string into date
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String birthdayString = formatter.format(inputDate);
                user.setBirthday(inputDate);

                // Print the date to the screen
                birthdayValue.setText(birthdayString);

            });

            // Get the nickname of the user
            user.getBirthday().thenAccept(data -> {
                runOnUiThread(() -> {
                    Date defaultDate;
                    if (data == null) {
                        defaultDate = new Date();
                    } else
                        defaultDate = data;

                    // Show the dialog
                    datePickerDialog.show("Edit Birthday", defaultDate);
                });
            });

        });

        // Homepage
        homepageValue.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    homepageValue.setText((String) input);
                    user.setHomepage((String) input);
                }
            });

            // Get the homepage of the user
            user.getHomepage().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Homepage", data);
                });
            });

        });

        // Address
        addressValue.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    addressValue.setText((String) input);
                    user.setAddress((String) input);
                }
            });

            // Get the address of the user
            user.getAddress().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Address", data);
                });
            });

        });

        // Phone number
        phoneNumberValue.setOnClickListener(v -> {
            // This should be a number's only
            PhonePickerDialog textDialog = new PhonePickerDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    // Format the number
                    String formattedNumber = PhonePickerDialog.formatPhoneNumber((String) input);

                    // If the format was successful
                    if (formattedNumber == null) {
                        // Invalid format
                        Toast.makeText(this, "Format: XXXXXXXXXX", Toast.LENGTH_SHORT).show();
                    } else {
                        phoneNumberValue.setText(formattedNumber);
                        user.setPhone((String) input);
                    }
                }
            });

            // Get the phone number of the user
            user.getPhone().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Phone Number", data);
                });
            });

        });

        // Email
        emailValue.setOnClickListener(v -> {
            // This should be a regular text view
            InputTextDialog textDialog = new InputTextDialog(this, input -> {
                // If the input is not null, set the name
                if (input != null) {
                    emailValue.setText((String) input);
                    user.setEmail((String) input);
                }
            });

            // Get the email of the user
            user.getEmail().thenAccept(data -> {
                runOnUiThread(() -> {
                    // Show the dialog
                    textDialog.show("Edit Email", data);
                });
            });

        });


        // Set up click listeners for the buttons
        eventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
            intent.putExtra("userId", user.getUID()); // Assuming userId is the ID of the current user
            startActivity(intent);
        });
        adminButton.setOnClickListener(v -> {
            // FOR TESTING
            // Load a specific QR code
            FirebaseAccess imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
            imageAccess.getImageFromFirestore("QRCD-1BS9WTNYYWXMVK9V7WGB", ImageType.eventQRCodes).thenAccept(imageData -> {
                Bitmap bitmap = (Bitmap) imageData.get("image");
            });

//            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
//            startActivity(intent);
        });
        scanQRCodeButton.setOnClickListener(v -> {
            // Switch to the QR code scanner
            Intent intent = new Intent(getApplicationContext(), QRScanner.class);
            qrCodeActivityResultLauncher.launch(intent);
        });


        // Set up click listener for the profile image
        profileImageView.setOnClickListener(v -> handleProfileImageClick());

        // Set up click listener for the geolocation toggle
        toggleGeolocationSwitch.setOnClickListener(v -> handleGeolocationToggled());
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
                    String birthdayString = formatter.format(birthday);
                    birthdayValue.setText(birthdayString);
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
        user.getAddress().thenAccept(address -> {
            runOnUiThread(() -> {
                // Update the fields
                if (address != null) {
                    addressValue.setText(address);
                }
            });
        });

        // Update the phone
        user.getPhone().thenAccept(phone -> {
            runOnUiThread(() -> {
                // Update the fields
                if (phone != null) {
                    String formattedNumber = PhonePickerDialog.formatPhoneNumber((String) phone);

                    // If the format was successful
                    if (formattedNumber != null) {
                        // Invalid format
                        phoneNumberValue.setText(formattedNumber);
                    }
                }
            });
        });

        // Update the email
        user.getEmail().thenAccept(email -> {
            runOnUiThread(() -> {
                // Update the fields
                if (email != null) {
                    emailValue.setText(email);
                }
            });
        });

        // Update the profile picture
        user.getProfileImage().thenAccept(image -> {
            runOnUiThread(() -> {
                // Update the fields
                if (image != null) {
                    ((ImageView) profileImageView).setImageBitmap(image);
                }
            });
        });

        // Update the geolocation
        user.getGeolocation().thenAccept(value -> {
            runOnUiThread(() -> {
                // Update the fields
                if (value != null) {
                    toggleGeolocationSwitch.setChecked(value);
                }
            });
        });
    }


    /**
     * Handles updating the profile picture when the profile image is clicked
     */
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
                                if (cameraEnabled) {
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

    // ChatGPT: How do I request location permission in android?

    /**
     * Handles the geolocation switch being toggled
     */
    private void handleGeolocationToggled() {
        // Check for geolocation permissions
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // At least one of the permissions are not granted, request them
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        } else {
            locationEnabled = true;
        }

        // Set the user's preference
        if (locationEnabled) {
            user.setGeolocation(toggleGeolocationSwitch.isChecked());
        } else {
            user.setGeolocation(false);
            toggleGeolocationSwitch.setChecked(false);

            // Show a toast
            if (!initialRun) {
                Toast.makeText(ProfileActivity.this, "Location Permission Required", Toast.LENGTH_LONG).show();
            }
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
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // At least one was granted
                    locationEnabled = true;
                } else {
                    // Location denyed
                    locationEnabled = false;
                }
                return;
            }
        }
    }

    /**
     * Shows the edit field dialog for a field on the page using its current value
     *
     * @param fieldName  The field we are editing
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