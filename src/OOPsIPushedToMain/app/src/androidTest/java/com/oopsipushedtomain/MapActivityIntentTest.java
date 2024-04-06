package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Geolocation.MapActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess db;

    /**
     * Manually request fine location permissions
     */
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    /**
     * Manually request coarse location permissions
     */
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    /**
     * Used to access the currently running MapActivity
     */
    @Rule
    public ActivityTestRule<MapActivity> mapActivityRule = new ActivityTestRule<>(MapActivity.class);

    /**
     * Used to access the currently running EventDetailsActivity
     */
    @Rule
    public ActivityTestRule<EventDetailsActivity> eventDetailsActivityRule = new ActivityTestRule<>(EventDetailsActivity.class);

    /**
     * US 03.02.01 - Enable or disable geolocation tracking
     * Create a new user and initialize the database. Enable geolocation tracking for the new user.
     */
    @Before
    public void setup() {
        // Create a new user and initialize the database
        try {
            user = User.createNewObject().get();
            user.setName("MapActivity tester");
            user.setGeolocation(true);
            db = new FirebaseAccess(FirestoreAccessType.EVENTS);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("SetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 01.08.01 - View check-ins on a map
     * Creates a new test event, opens it, verifies that there are no markers on the map.
     * Then close the map, manually check-in to the event, and verify that there is now a marker.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testMapActivity() throws InterruptedException {
        // Launch EventListActivity
        Intent i = new Intent(getInstrumentation().getTargetContext(), EventListActivity.class);
        i.putExtra("userId", user.getUID());
        ActivityScenario.launch(i);//.onActivity(activity -> {});

        // Create a test event
        String titleToType = "MapActivity test event " + (Math.random() * 10 + 1);
        String startDateToType = "2024-01-01";
        String endDateToType = "2024-01-02";
        String descriptionToType = "Event to test MapActivity";
        onView(withId(R.id.create_event_button)).perform(click());
        onView(withId(R.id.new_event_title_e)).perform(ViewActions.typeText(titleToType));
        onView(withId(R.id.new_event_start_time_e)).perform(ViewActions.typeText(startDateToType));
        onView(withId(R.id.new_event_end_time_e)).perform(ViewActions.typeText(endDateToType));
        onView(withId(R.id.new_event_description_e)).perform(ViewActions.typeText(descriptionToType));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());

        // Open new event, open map, check that there are no markers
        onView(withText(containsString(titleToType))).perform(click());
        onView(withId(R.id.btnViewMap)).perform(click());
        Thread.sleep(2000); // Give the map some time to load markers
        MapActivity mapActivity = mapActivityRule.getActivity();
        assertEquals(0, mapActivity.getMarkerCount());

        // Close map, check in to event, wait for DB to update
        Espresso.pressBack();
        EventDetailsActivity eventDetailsActivity = eventDetailsActivityRule.getActivity();
        eventId = eventDetailsActivity.getEventID();
        user.checkIn(eventId, eventDetailsActivity.getApplicationContext());
        Thread.sleep(5000);  // Wait for DB to update

        // Open map
        onView(withId(R.id.btnViewMap)).perform(click());

        // Check that there now is a marker
        Thread.sleep(2000); // Give the map some time to load markers
        mapActivity = mapActivityRule.getActivity();
        assertEquals(1, mapActivity.getMarkerCount());
    }

    /**
     * Clean up the database by deleting the test user and test event.
     */
    @After
    public void cleanUp() {
        // Delete the user and event
        try {
            user.deleteUser().get();
            db.deleteDataFromFirestore(eventId, FirebaseInnerCollection.checkInCoords, null);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestGetData", "Error: " + e.getMessage());
            fail();
        }
    }
}
