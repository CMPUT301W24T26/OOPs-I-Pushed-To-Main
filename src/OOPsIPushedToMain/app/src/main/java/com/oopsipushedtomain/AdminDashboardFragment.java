/**
 * This file contains the fragment for the admin dashboard
 * It allows the admin to choose what task they want to perform
 */
package com.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * AdminDashboardFragment represents the dashboard for administrators.
 * It provides functionality for browsing events, profiles, and images.
 */
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

        // Click listener for browsing events
        btnBrowseEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start EventListActivity to browse events
                Intent intent = new Intent(getActivity(), EventListActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            }
        });

        // Click listener for browsing profiles
        btnBrowseProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start ProfileListActivity to browse profiles
                Intent intent = new Intent(getActivity(), ProfileListActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            }
        });

        // Click listener for browsing images
        btnBrowseImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to list of images using ImageSelectionFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ImageSelectionFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}
