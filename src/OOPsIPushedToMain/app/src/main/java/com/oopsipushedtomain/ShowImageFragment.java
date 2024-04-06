package com.oopsipushedtomain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * A fragment for showing a given Bitmap image
 */
public class ShowImageFragment extends Fragment {

    /**
     * The image to show as a Bitmap
     */
    private Bitmap image;

    /**
     * Empty constructor
     */
    public ShowImageFragment() {
        // Required empty public constructor
    }


    /**
     * Creates a new instance of this fragment
     *
     * @param image The image that you want to show
     * @return The instance of the fragment
     */
    public static ShowImageFragment newInstance(Bitmap image) {
        ShowImageFragment fragment = new ShowImageFragment();

        // Add to bundle
        Bundle args = new Bundle();
        args.putParcelable("image", image);

        // Set arguments and return
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Loads the image to the fragment and sets the on click listeners
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return A reference to the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_image, container, false);

        ImageView imageView = view.findViewById(R.id.image_view);

        // Retrieve the bitmap from arguments
        if (getArguments() != null) {
            image = getArguments().getParcelable("image");
            imageView.setImageBitmap(image);
        }

        Button shareButton = view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> {
            if (image != null) {
                try {
                    // Save the bitmap to cache and get its URI
                    File cachePath = new File(getContext().getCacheDir(), "images");
                    cachePath.mkdirs(); // Don't forget to make the directory
                    FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // Overwrites this image every time
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    File imagePath = new File(getContext().getCacheDir(), "images");
                    File newFile = new File(imagePath, "image.png");
                    Uri contentUri = FileProvider.getUriForFile(getContext(), "com.oopsipushedtomain.fileprovider", newFile);

                    if (contentUri != null) {
                        // Create an intent to share the image
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Temporarily grant read permission
                        // shareIntent.setDataAndType(contentUri, getContext().getContentResolver().getType(contentUri)); // This line might not be necessary and could be removed or modified.
                        shareIntent.setType(getContext().getContentResolver().getType(contentUri));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        startActivity(Intent.createChooser(shareIntent, "Choose an app"));


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to share image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

}