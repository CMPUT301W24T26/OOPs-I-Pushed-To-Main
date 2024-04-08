package com.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

//    private void fetchProfiles() {
//        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
//        firebaseAccess.getAllDocuments()
//                .thenAccept(usersData -> {
//                    if (usersData != null) {
//                        // Create a list of futures using the stream
//                        List<CompletableFuture<User>> futures = usersData.stream()
//                                .map(userData -> User.createNewObject((String) userData.get("UID")))
//                                .collect(Collectors.toList());
//
//                        // Combine all futures into a single CompletableFuture
//                        CompletableFuture<List<User>> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                                .thenApply(v -> futures.stream()
//                                        .map(CompletableFuture::join)
//                                        .collect(Collectors.toList())
//                                );
//
//                        // Handle the final result once all futures are completed
//                        allFutures.thenAccept(tempUserList -> {
//                            runOnUiThread(() -> {
//                                userList.clear();
//                                userList.addAll(tempUserList);
//                                userAdapter.notifyDataSetChanged();
//                                Log.d("UserListActivity", "Adapter notified with user list size: " + userList.size());
//                            });
//                        }).exceptionally(exception -> {
//                            Log.e("UserListActivity", "Error processing user futures", exception);
//                            return null;
//                        });
//                    } else {
//                        Log.d("UserListActivity", "No users data received.");
//                    }
//                })
//                .exceptionally(exception -> {
//                    Log.e("UserListActivity", "Error fetching users from Firestore: ", exception);
//                    return null;
//                });
//    }

    private void fetchProfiles() {
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
        firebaseAccess.getAllDocuments()
                .thenAcceptAsync(usersData -> { // Run on a background thread
                    if (usersData != null && !usersData.isEmpty()) {
                        showLoadingIndicator(true); // Might need to ensure this runs on UI thread if necessary

                        List<User> users = new ArrayList<>();
                        for (Map<String, Object> userData : usersData) {
                            try {
                                String uid = (String) userData.get("UID");
                                User user = User.createNewObject(uid).join(); // Block and wait
                                if (user != null) {
                                    users.add(user);
                                }
                            } catch (Exception ex) {
                                Log.e("UserListActivity", "Failed to create user object", ex);
                            }
                        }

                        // Update UI on the main thread
                        runOnUiThread(() -> {
                            userList.clear();
                            userList.addAll(users);
                            userAdapter.notifyDataSetChanged();
                            showLoadingIndicator(false);
                        });
                    } else {
                        showLoadingIndicator(false);
                    }
                })
                .exceptionally(exception -> {
                    Log.e("UserListActivity", "Error fetching users from Firestore", exception);
                    showLoadingIndicator(false);
                    return null;
                });
    }

    // Also, update the showLoadingIndicator method to make sure it's accessing the progressBar on the UI thread.
    private void showLoadingIndicator(final boolean show) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }



}
