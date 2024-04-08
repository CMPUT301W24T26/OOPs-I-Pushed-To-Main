package com.oopsipushedtomain.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.oopsipushedtomain.R;

/**
 * A Fragment for selecting images from different categories such as event pictures and profile pictures.
 * Provides UI for the user to navigate between different image categories.
 */
public class ImageSelectionFragment extends Fragment {

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_image_selection_fragment, container, false);

        // Initialize buttons for selecting event and profile pictures.
        Button btnEventPictures = view.findViewById(R.id.btnEventPictures);
        Button btnProfilePictures = view.findViewById(R.id.btnProfilePictures);

        // Set onClick listeners for each button to handle user interaction.
        btnEventPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ImageListActivity.class);
                intent.putExtra("IMAGES_TYPE", "events");
                startActivity(intent);

            }
        });

        btnProfilePictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ImageListActivity.class);
                intent.putExtra("IMAGES_TYPE", "profiles");
                startActivity(intent);
            }
        });

        return view;
    }
}
