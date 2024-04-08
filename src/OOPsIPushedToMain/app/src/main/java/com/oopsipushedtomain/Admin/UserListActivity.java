package com.oopsipushedtomain.Admin;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    UserListAdapter userListAdapter;
    /**
     * List to store profiles
     */
    private List<Profile> profilesList = new ArrayList<>();

    public static class Profile{
        private String uid;
        private String address;
        private Date birthday;
        private String email;
        private String homepage;
        private String name;
        private String nickname;
        private String phone;
        private String profileImageUID;
        private Bitmap profileImage;
        private String fid;
        private boolean dataIsCurrent;
        private boolean geolocation;

        // Default constructor
        public Profile() {
        }

        /**
         * Getters and Setters
         */

        public void setUID(String uid) {
            this.uid = uid;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getProfileImageUID() {
            return profileImageUID;
        }

        public void setProfileImageUID(String profileImageUID) {
            this.profileImageUID = profileImageUID;
        }

        public Bitmap getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(Bitmap profileImage) {
            this.profileImage = profileImage;
        }

        public String getFid() {
            return fid;
        }

        public void setFid(String fid) {
            this.fid = fid;
        }

        public boolean isDataIsCurrent() {
            return dataIsCurrent;
        }

        public void setDataIsCurrent(boolean dataIsCurrent) {
            this.dataIsCurrent = dataIsCurrent;
        }

        public boolean isGeolocation() {
            return geolocation;
        }

        public void setGeolocation(boolean geolocation) {
            this.geolocation = geolocation;
        }

        public String getUID() {
            return uid;
        }
    }

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
        setContentView(R.layout.activity_user_list);

        userRecyclerView = findViewById(R.id.profilesRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with profilesList using a lambda expression
        userListAdapter = new UserListAdapter(this, profilesList, profile -> showDeleteConfirmationDialog(profile));

        userRecyclerView.setAdapter(userListAdapter);
        fetchProfiles();
    }

    /**
     * Shows a confirmation dialog for deleting a user
     * @param profile The user selected
     */
    private void showDeleteConfirmationDialog(Profile profile) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete this profile?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteProfile(profile);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }


    /**
     * Fetches profiles from the database and updates the UI accordingly.
     */
    private void fetchProfiles() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                profilesList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Profile profile = document.toObject(Profile.class);
                    profile.setUID(document.getId()); // Set the UID for the profile
                    profilesList.add(profile);
                }
                userListAdapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Log.w("UserListActivity", "Error getting documents.", task.getException());
                Toast.makeText(UserListActivity.this, "Error loading profiles.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a profile on the database
     * @param profile The profile to delete
     */
    public void deleteProfile(Profile profile) {
        // Assuming FirebaseAccess class is equipped to handle deletion and user.getUid() is accessible directly
        FirebaseAccess firebaseAccess = new FirebaseAccess(FirestoreAccessType.USERS);
        String userId = profile.getUID(); // Ensure getUid() method exists and retrieves the user's ID

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


}
