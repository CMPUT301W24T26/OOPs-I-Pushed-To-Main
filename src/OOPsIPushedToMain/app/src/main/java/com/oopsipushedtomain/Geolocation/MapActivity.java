package com.oopsipushedtomain.Geolocation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * This activity displays an OpenStreetMaps map showing markers of where attendees have checked
 * in from. The event ID is passed in as a string through an Intent from EventDetailsActivity,
 * which is then used to query the checkInCoords inner collection to get the markers.
 *
 * @author Aidan Gironella
 * @see com.oopsipushedtomain.EventDetailsActivity
 */
public class MapActivity extends AppCompatActivity {
    /**
     * OpenStreetMaps MapView object
     */
    private MapView map;

    /**
     * ID of the event we're working with
     */
    private String eventId;

    /**
     * Database class
     */
    private FirebaseAccess db;

    /**
     * Stores the number of markers on the map
     */
    private int markerCount;

    /**
     * Tag used for log messages
     */
    private final String TAG = "Geolocation";

    /**
     * Creates and instantiates the activity.
     * Sets up the map, including the initial location it opens to, then calls setMarkers()
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it
     *                           most recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load/initialize the osmdroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Inflate and create the map
        setContentView(R.layout.activity_map);

        // Initialize the map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        map.getZoomController().activate();
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();

        // Set the initial location that the map opens to
        GeoPoint startPoint = new GeoPoint(53.5271616,-113.5288948);
        mapController.setZoom(16);
        mapController.setCenter(startPoint);

        // Read the event ID from the intent
        eventId = getIntent().getStringExtra("eventId");

        // Initialize the database object and get the event's checkInCoords
        db = new FirebaseAccess(FirestoreAccessType.EVENTS);
        setMarkers();
    }

    /**
     * Uses the FirebaseAccess class to obtain the inner collection checkInCoords for the provided
     * event ID. Iterates over the coordinates received and sets a marker on the map for each one.
     */
    private void setMarkers() {
        // Uses the database class to obtain the markers in an ArrayList<Map<String, Object>>
        this.markerCount = 0;
        db.getAllDocuments(eventId, FirebaseInnerCollection.checkInCoords).thenAccept(markers -> {
            if (markers == null) {
                Log.d(TAG, "No markers found");
            } else {
                Log.d(TAG, "Obtained markers");

                // Iterate over each marker, which is currently a Map<String, Object>
                for (Map<String, Object> marker : markers) {
                    // Cast the marker to a Firebase GeoPoint object so that we can easily get its
                    // latitude and longitude
                    com.google.firebase.firestore.GeoPoint fbGeoPoint =
                            (com.google.firebase.firestore.GeoPoint) marker.get("coordinates");
                    double lat = Objects.requireNonNull(fbGeoPoint).getLatitude();
                    double lon = fbGeoPoint.getLongitude();

                    // Create a new OpenStreetMaps GeoPoint using the obtained coordinates
                    GeoPoint point = new GeoPoint(lat, lon);

                    // Create a new OpenStreetMaps marker object and set it's position
                    Marker mapMarker = new Marker(map);
                    mapMarker.setPosition(point);
                    mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(mapMarker);

                    // Get the userId and timestamp of the check-in, and set it as the marker title
                    String userId = (String) marker.get("userId");
                    Date timestamp = ((com.google.firebase.Timestamp) Objects.requireNonNull(marker.get("timestamp"))).toDate();
                    String title = String.format("User ID: %s\nTimestamp: %s", userId, timestamp);
                    mapMarker.setTitle(title);
                    this.markerCount++;
                }
            }
        });
    }

    /**
     * Returns the number of markers on the map. Used for intent testing.
     * @return Number of markers currently on the map.
     */
    public int getMarkerCount() {
        return this.markerCount;
    }

    /**
     * Handles when permissions are requested
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if (!permissionsToRequest.isEmpty()) {
            int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}