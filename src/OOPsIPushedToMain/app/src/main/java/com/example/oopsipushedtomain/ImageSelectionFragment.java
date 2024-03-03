package com.example.oopsipushedtomain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class ImageSelectionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_image_selection_fragment, container, false);

        Button btnEventPictures = view.findViewById(R.id.btnEventPictures);
        Button btnProfilePictures = view.findViewById(R.id.btnProfilePictures);

        btnEventPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getActivity(), ListEventPicturesActivity.class);

                // Send flag that admin is accessing
                // intent.putExtra("isAdmin", true);

                //startActivity(intent);
            }
        });

        btnProfilePictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getActivity(), ListProfilesPicturesActivity.class);

                // Send flag that admin is accessing
                // intent.putExtra("isAdmin", true);

                //startActivity(intent);
            }
        });

        return view;
    }
}
