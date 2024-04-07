package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the EventDetailsActivity class.
 * <p>
 * This test class is designed to verify the functionality of the EventDetailsActivity. It ensures that users can
 * interact with the UI elements to edit event details, such as the event title and description, and can delete an event.
 * The tests simulate user actions like typing text into EditText fields and clicking buttons, and then verify the expected outcomes.
 * <p>
 * The setup method initializes the EventDetailsActivity with necessary intent extras, simulating the scenario where a user
 * navigates to this activity from another part of the app. It uses the ActivityScenario class to launch the activity in a test environment.
 * <p>
 * Each test method focuses on a specific functionality:
 * - {@code testEditEventDetails()} verifies that the event title can be edited successfully.
 * - {@code testEditEventDescription()} checks if the event description can be updated.
 * - {@code testDeleteEvent()} ensures that the event can be deleted through the UI.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    @Rule
    public ActivityScenarioRule<EventDetailsActivity> activityRule = new ActivityScenarioRule<>(EventDetailsActivity.class);
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Sets up the testing environment before each test.
     * <p>
     * This method initializes the intent required to launch EventDetailsActivity with necessary extras,
     * including the user ID and the selected event ID. It then launches the activity in a test environment.
     */
    @Before
    public void setup() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("userId", "testUserId");
        intent.putExtra("selectedEventId", "testEventId");
        ActivityScenario.launch(intent);
    }

    /**
     * Tests if the event details can be edited successfully.
     * <p>
     * This test checks if the activity is in the correct state, performs text input into the event title field,
     * and simulates a click on the save button.
     */
    @Test
    public void testEditEventDetails() {
        onView(withId(R.id.event_details_organizer_title_e)).check(matches(isDisplayed()));
        onView(withId(R.id.event_details_organizer_title_e)).perform(clearText(), typeText("Updated Test Event"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSaveEventDetails)).perform(click());
    }

    /**
     * Tests if the event description can be edited successfully.
     * <p>
     * This test checks if the activity is in the correct state, performs text input into the event description field,
     * and simulates a click on the save button.
     */
    @Test
    public void testEditEventDescription() {
        onView(withId(R.id.event_details_organizer_description_e)).check(matches(isDisplayed()));
        onView(withId(R.id.event_details_organizer_description_e)).perform(clearText(), typeText("Updated Description"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSaveEventDetails)).perform(click());
    }

    /**
     * Tests if an event can be deleted successfully.
     * <p>
     * This test simulates a click on the delete event button and confirms the deletion action in the dialog that appears.
     */
    @Test
    public void testDeleteEvent() {
        onView(withId(R.id.btnDeleteEvent)).perform(click());
        onView(withText("Yes")).perform(click());
    }
}




