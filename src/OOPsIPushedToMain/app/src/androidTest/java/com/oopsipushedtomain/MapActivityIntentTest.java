package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import android.Manifest;
import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.overlay.Marker;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityIntentTest {
    /**
     * Manually request fine location permissions
     */
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    /**
     * Manually request coarselocation permissions
     */
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    @Test
    public void testMapActivty() throws InterruptedException {
        // Launch EventListActivity with TEST-USER passed as the userId
        Intent i = new Intent(getInstrumentation().getTargetContext(), EventListActivity.class);
        i.putExtra("userId", "MAP-ACTIVITY-TEST-USER");
        ActivityScenario.launch(i).onActivity(activity -> {
        });

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

        // Open new event
        onView(withText(containsString(titleToType))).perform(click());

        // Open map
        onView(withId(R.id.btnViewMap)).perform(click());

        // Check that there are no markers
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        Thread.sleep(2000); // Give the map some time to load markers
        UiObject marker = device.findObject(new UiSelector().className(Marker.class));
        assertFalse(marker.exists());

        // Close map
        Espresso.pressBack();

        // Sign up for event and wait for geolocation to kick in
        onView(withId(R.id.btnSignUpEvent)).perform(click());
        Thread.sleep(5000);

        // Open map
        onView(withId(R.id.btnViewMap)).perform(click());

        // Check that there now is a marker
        Thread.sleep(2000); // Give the map some time to load markers
        marker = device.findObject(new UiSelector().className(Marker.class));
        assertTrue(marker.exists());

        // Close map and delete event
        Espresso.pressBack();
        onView(withId(R.id.btnDeleteEvent)).perform(click());
    }
}
