package com.oopsipushedtomain.Database;

import android.util.Log;

import com.google.firebase.installations.FirebaseInstallations;

/**
 * Helper function to quickly get the Firebase Installation ID (fid)
 */
public final class GetFIDUtil {
    /**
     * General constructor
     */
    private GetFIDUtil(){};

    /**
     * The fid of the user
     */
    private static String fid = "";

    /**
     * Interface that allows for synchronization
     */
    public interface DataLoadedListener {
        /**
         * Sends the fid for the user to the calling function
         * @param fid The user's FID
         */
        void onDataLoaded(String fid);
    }

    /**
     * Obtains the Firebase Installation ID and stores it in fid, then lets the listener now
     * to proceed
     * @param listener DataLoadedListener that controls if the app can proceed
     */
    public static void GetFID(DataLoadedListener listener) {
        // Get the FID
        FirebaseInstallations.getInstance().getId().addOnCompleteListener(getFIDTask -> {
            if (getFIDTask.isSuccessful()) {
                // Store the FID in a String
                fid = getFIDTask.getResult();
                Log.d("GetFIDUtil", "FID is " + fid);
            } else {
                Log.e("GetFIDUtil", "Couldn't get FID");
            }
            // Let the app know it can proceed by passing back the FID
            listener.onDataLoaded(fid);
        });
    }
}
