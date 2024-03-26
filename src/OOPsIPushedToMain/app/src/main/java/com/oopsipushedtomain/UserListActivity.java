package com.oopsipushedtomain;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UserListActivity is responsible for displaying a list of users retrieved from a database.
 * It utilizes a RecyclerView to display the users using UserListAdapter.
 */
public class UserListActivity extends AppCompatActivity {
    /**
     * RecyclerView to display users
     */
    RecyclerView usersRecyclerView;
    /**
     * Adapter for users
     */
    UserListAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    /**
     * Initializes the parameters of the class
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // Layout for activity

        /**
         * List to store users
         */

        usersRecyclerView = findViewById(R.id.usersRecyclerView); // Initialize RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Initialize adapter and set it to RecyclerView
        userAdapter = new UserListAdapter(this, userList, new OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                showDeleteConfirmationDialog(user);
            }
        });

        usersRecyclerView.setAdapter(userAdapter); // Don't forget to set the adapter to the RecyclerView

        fetchUsers(); // Fetch users from Firestore and populate the RecyclerView
    }

    /**
     * Shows a confirmation dialog for deleting a users
     *
     * @param user The user selected
     */
    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this User?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteUser(user);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Deletes a specified user from the database. This method utilizes the FirebaseAccess class
     * to perform the deletion based on the user's unique identifier (UID). It is designed to delete the
     * user without requiring additional parameters for inner collections or documents, simplifying the deletion process.
     *
     * @param user The User object representing the user to be deleted. It must contain a valid UID.
     *                This method directly uses the UID of the provided User object to identify and delete
     *                the corresponding user in the database.
     */
    private void deleteUser(User user) {
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
        firebaseAccess.deleteDataFromFirestore(user.getUid(), null, null); // Assuming method signature might need adjusting

        // Note: If the FirebaseAccess class's deleteDataFromFirestore method is asynchronous and does not
        // provide a mechanism for success/failure callbacks, you may need to implement these callbacks
        // within the FirebaseAccess class or adapt your application's architecture to handle such cases.
    }

    /**
     * Fetches and loads users from the database asynchronously. This method creates a background
     * thread to query the database using the FirebaseAccess class, specifically targeting the USERS collection.
     * Each retrieved user is transformed into a User object, and the list of User objects is then
     * used to update the UI. This method ensures that UI updates are performed on the UI thread, adhering
     * to Android's threading model.
     */
    private void fetchUsers() {
        new Thread(() -> {
            try {
                FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
                ArrayList<Map<String, Object>> userMaps = firebaseAccess.getAllDocuments(null, null);

                // Transform each map into a User object and collect them into a temporary list
                List<User> tempList = new ArrayList<>();
                for (Map<String, Object> userMap : userMaps) {
                    tempList.add(new User(userMap));
                }

                // Update the UI with the fetched users, ensuring the operation is performed on the UI thread
                runOnUiThread(() -> {
                    userList.clear();
                    userList.addAll(tempList);
                    userAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e("UserListActivity", "Error fetching users: ", e);
                runOnUiThread(() -> Toast.makeText(UserListActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

