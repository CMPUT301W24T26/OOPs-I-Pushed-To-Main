package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity class to display a list of images from Firebase.
 * It allows users to view images categorized as either events or profiles,
 * and offers functionality to delete a selected image.
 */

public class ImageListActivity extends AppCompatActivity {
    /**
     * RecyclerView to display the list of images.
     */
    RecyclerView recyclerView;
    /**
     * Adapter to manage the display and interaction of image items in the RecyclerView.
     */
    private ImageAdapter adapter;

    /**
     * List to hold image information objects.
     */
    private List<ImageInfo> imageInfos = new ArrayList<>();

    /**
     * Initializes the activity, sets up the RecyclerView and its adapter,
     * and decides whether to fetch event or profile images based on intent extras.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);



        recyclerView = findViewById(R.id.imagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageAdapter(this, imageInfos, position -> {
            // Show confirmation dialog
            new AlertDialog.Builder(this).setTitle("Delete Image").setMessage("Are you sure you want to delete this image?").setPositiveButton("Yes", (dialog, which) -> deleteImage(position)).setNegativeButton("No", null).show();
        });
        recyclerView.setAdapter(adapter);

        recyclerView.setAdapter(adapter);

        // Check the intent for what to display
        String IMAGES_TYPE = getIntent().getStringExtra("IMAGES_TYPE");
        if ("events".equals(IMAGES_TYPE)) {
            fetchAllEventsAndDisplayImages();
        } else if ("profiles".equals(IMAGES_TYPE)) {
            fetchAllUsersAndDisplayImages();
        }

    }
    private void fetchAllEventsAndDisplayImages() {
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.EVENTS);

        firebaseAccess.getAllRelatedImagesFromFirestore(null, ImageType.eventPosters)
                .thenAccept(imageMaps -> {
                    if (imageMaps != null) {
                        for (Map<String, Object> imageMap : imageMaps) {
                            Bitmap imageBitmap = FirebaseAccess.blobToBitmap((Blob) imageMap.get("image"));
                            String imageUID = (String) imageMap.get("UID");

                            // Create ImageInfo objects and add them to adapter
                            ImageInfo imageInfo = new ImageInfo(imageBitmap, /* imagePath or storagePath */, imageUID);
                            imageInfos.add(imageInfo);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                }).exceptionally(exception -> {
                    Log.e("ImageListActivity", "Error fetching event images", exception);
                    return null;
                });
    }

    private void fetchAllUsersAndDisplayImages() {
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);

        firebaseAccess.getAllRelatedImagesFromFirestore(null, ImageType.profilePictures)
                .thenAccept(imageMaps -> {
                    if (imageMaps != null) {
                        for (Map<String, Object> imageMap : imageMaps) {
                            Bitmap imageBitmap = FirebaseAccess.blobToBitmap((Blob) imageMap.get("image"));
                            String imageUID = (String) imageMap.get("UID");

                            // Create ImageInfo objects and add to adapter
                            ImageInfo imageInfo = new ImageInfo(imageBitmap, /* imagePath or storagePath */, imageUID);
                            imageInfos.add(imageInfo);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                }).exceptionally(exception -> {
                    Log.e("ImageListActivity", "Error fetching user images", exception);
                    return null;
                });
    }

    private void deleteImage(int position) {
        ImageInfo imageInfo = imageInfos.get(position);
        String imageUID = imageInfo.getFirestoreDocumentId();

        FirebaseAccess firebaseAccess = new FirebaseAccess(/* FirestoreAccessType for image type */);

        firebaseAccess.deleteImageFromFirestore(/* outerDocName */, imageUID, /* ImageType */)
                .thenRun(() -> {
                    runOnUiThread(() -> {
                        imageInfos.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                }).exceptionally(exception -> {
                    Log.e("ImageListActivity", "Error deleting image", exception);
                    runOnUiThread(() -> Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show());
                    return null;
                });
    }
}
