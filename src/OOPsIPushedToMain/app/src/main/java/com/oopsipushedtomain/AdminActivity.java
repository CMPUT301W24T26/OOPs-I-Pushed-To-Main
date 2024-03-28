/**
 * This file will show the fragment containing the admin controls.
 * It is the main activity for admin tasks
 */

package com.oopsipushedtomain;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


/**
 * AdminActivity is the entry point for administrators.
 * It hosts the AdminDashboardFragment to provide administrative functionalities.
 */
public class AdminActivity extends AppCompatActivity {

    /**
     * Creates the AdminDashboardFragment
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Replace placeholder fragment container with AdminDashboardFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminDashboardFragment()).commit();
        }
    }
}
