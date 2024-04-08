package com.oopsipushedtomain;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Geolocation.MapActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Intent test for the MapActivity. Covers the following user stories:
 * US 01.08.01 - View check-ins on a map
 * US 03.02.01 - Enable or disable geolocation tracking
 *
 * @author Aidan Gironella
 * @see MapActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess db;

    /**
     * Manually request all permissions so that the dialogs don't appear
     */
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);
    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    /**
     * Used to access the currently running ProfileActivity
     */
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    /**
     * Used to access the currently running MapActivity
     */
    @Rule
    public ActivityTestRule<MapActivity> mapActivityRule = new ActivityTestRule<>(MapActivity.class);

    /**
     * Initialize the databases.
     */
    @Before
    public void setup() {
        // Initialize the database
        try {
            Intents.init();
            db = new FirebaseAccess(FirestoreAccessType.EVENTS);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("MapActivityIntentTestSetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 01.08.01 - View check-ins on a map
     * US 03.02.01 - Enable or disable geolocation tracking
     * Creates a new test event, opens it, verifies that there are no markers on the map.
     * Then close the map, manually check-in to the event, and verify that there is now a marker.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testMapActivity() throws InterruptedException {
        int startupDelay = 1000;
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());
        onView(withId(R.id.eventsButton)).perform(click());

        // Create new test event
        createNewTestEvent();

        // Intercept the event ID intent extra so that we can delete the event in cleanUp()
        Thread.sleep(2000);
        List<Intent> intents = Intents.getIntents();
        for (Intent intent : intents)
            // Get the extra data from the intent
            eventId = intent.getStringExtra("selectedEventId");

        // Release Intents
        Intents.release();

        // Open map, check that there are no markers
        onView(withId(R.id.btnViewMap)).perform(click());
        Thread.sleep(2000); // Give the map some time to load markers
        MapActivity mapActivity = mapActivityRule.getActivity();
        assertEquals(0, mapActivity.getMarkerCount());

        // Close map, check-in to event WITHOUT enabling geolocation, check map is still empty
        Espresso.pressBack();
        user.checkIn(eventId, getApplicationContext());
        Thread.sleep(5000);  // Wait for DB to update
        onView(withId(R.id.btnViewMap)).perform(click());
        Thread.sleep(2000); // Give the map some time to load markers
        mapActivity = mapActivityRule.getActivity();
        assertEquals(0, mapActivity.getMarkerCount());

        // Close map, enable geolocation, check in to event, wait for DB to update
        Espresso.pressBack();
        user.setGeolocation(true);
        user.checkIn(eventId, getApplicationContext());
        Thread.sleep(5000);  // Wait for DB to update

        // Open map
        onView(withId(R.id.btnViewMap)).perform(click());

        // Check that there now is a marker
        Thread.sleep(2000); // Give the map some time to load markers
        mapActivity = mapActivityRule.getActivity();
        assertEquals(1, mapActivity.getMarkerCount());
    }

    /**
     * Helper function to fill all the fields to create a new test event
     */
    private void createNewTestEvent() {
        // Set title
        String titleToType = "MapActivity test event " + (Math.random() * 10 + 1);
        onView(withId(R.id.create_event_button)).perform(click());
        onView(withId(R.id.new_event_title_e)).perform(ViewActions.typeText(titleToType));
        Espresso.closeSoftKeyboard();
        // Set start date and time
        onView(withId(R.id.edit_event_start_button)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withText("OK")).perform(click());
        // Set end date and time
        onView(withId(R.id.edit_event_end_button)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withText("OK")).perform(click());
        // Set description
        String descToType = "MapActivity intent test event";
        onView(withId(R.id.new_event_description_e)).perform(ViewActions.typeText(descToType));
        Espresso.closeSoftKeyboard();
        // Limit attendees to 10
        onView(withId(R.id.new_event_attendee_limit_e)).perform(ViewActions.typeText("5"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());
    }

    /**
     * Clean up the database by deleting the test user and test event.
     */
    @After
    public void cleanUp() {
        // Delete the user
        try {
            user.deleteUser().get();
            db.deleteDataFromFirestore(eventId).get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("MapActivityIntentTest", "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
