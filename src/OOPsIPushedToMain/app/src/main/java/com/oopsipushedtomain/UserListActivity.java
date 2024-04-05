package com.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProfileListActivity is responsible for displaying a list of user profiles retrieved from a database.
 * It utilizes a RecyclerView to display the profiles using ProfileListAdapter.
 */
public class UserListActivity extends AppCompatActivity {
    /**
     * RecyclerView to display profiles
     */
    RecyclerView userRecyclerView;
    /**
     * Adapter for profiles
     */
    UserListAdapter userAdapter;
    /**
     * List to store profiles
     */
    private List<User> userList = new ArrayList<>();

    /**
     * Initializes the parameters of the class
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // Layout for activity

        userRecyclerView = findViewById(R.id.profilesRecyclerView); // Initialize RecyclerView
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Initialize adapter and set it to RecyclerView
        userAdapter = new UserListAdapter(this, userList, new OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                showDeleteConfirmationDialog(user);
            }
        });

        userRecyclerView.setAdapter(userAdapter); // Don't forget to set the adapter to the RecyclerView

        fetchProfiles(); // Fetch profiles from Firestore and populate the RecyclerView
    }

    /**
     * Shows a confirmation dialog for deleting a user
     * @param user The user selected
     */
    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete this profile?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteProfile(user);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Deletes a profile on the database
     * @param user The profile to delete
     */
    private void deleteProfile(User user) {
        // Assuming FirebaseAccess class is equipped to handle deletion and user.getUid() is accessible directly
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
        String userId = user.getUID(); // Ensure getUid() method exists and retrieves the user's ID

        // Assuming FirebaseAccess has a method deleteDataFromFirestore that returns CompletableFuture<Void>
        firebaseAccess.deleteDataFromFirestore(userId)
                .thenRun(() -> {
                    // This block is executed upon successful deletion
                    runOnUiThread(() -> {
                        fetchProfiles(); // Refresh the list after deletion
                        Toast.makeText(UserListActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                })
                .exceptionally(e -> {
                    // This block is executed upon failure to delete
                    runOnUiThread(() -> Toast.makeText(UserListActivity.this, "Error deleting user", Toast.LENGTH_SHORT).show());
                    return null; // CompletableFutures expect a return, return null for void methods
                });
    }


    /**
     * Method to fetch profiles from the database.
     * Uses Firebase-Firestore to query the "users" collection.
     */
    private void fetchProfiles() {
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
        firebaseAccess.getAllDocuments() // This should be adjusted to match the actual method signature
                .thenAccept(usersData -> {
                    if (usersData != null) {
                        List<User> tempUserList = new ArrayList<>();
                        for (Map<String, Object> userData : usersData) {
                            tempUserList.add(User.createFromMap(userData));
                        }
                        runOnUiThread(() -> {
                            userList.clear(); // Assuming profileList is now of type List<User>
                            userList.addAll(tempUserList);
                            userAdapter.notifyDataSetChanged(); // Make sure your adapter is compatible with User objects
                        });
                    } else {
                        Log.d("UserListActivity", "Error getting documents or no documents found.");
                    }
                }).exceptionally(exception -> {
                    Log.e("UserListActivity", "Error fetching users", exception);
                    return null;
                });
    }

}
