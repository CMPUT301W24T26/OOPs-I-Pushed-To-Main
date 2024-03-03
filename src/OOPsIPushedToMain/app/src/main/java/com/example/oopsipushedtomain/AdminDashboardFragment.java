package com.example.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.Button;

import com.example.oopsipushedtomain.R;

import java.util.Objects;

public class AdminDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.admin_dashboard_fragment, container, false);

        Button btnBrowseEvents = view.findViewById(R.id.btnBrowseEvents);
        Button btnBrowseProfiles = view.findViewById(R.id.btnBrowseProfiles);
        Button btnBrowseImages = view.findViewById(R.id.btnBrowseImages);

        // Set up button click listeners
        btnBrowseEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent intent = new Intent(getActivity(), ListEventsActivity.class);

                // Send flag that admin is accessing
                // intent.putExtra("isAdmin", true);

                // startActivity(intent);
            }


            //Sample receive code
            // Retrieve the data passed through the intent
            //    boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);
            //
            //    if (isAdmin) {
            //        // Customize the UI or functionality for the admin
            //    }
        });

        btnBrowseProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getActivity(), ListProfilesActivity.class);

                // Send flag that admin is accessing
                // intent.putExtra("isAdmin", true);

                //startActivity(intent);
            }
        });

        btnBrowseImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Navigate to list of images
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ImageSelectionFragment());
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        return view;
    }
}
