package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
import androidx.test.rule.GrantPermissionRule;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Intent test for signing up for events. Covers the following user stories:
 * US 02.07.01 - Sign up to attend an event
 * US 02.09.01 - Know signed up events
 *
 * @author Aidan Gironella
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess db;
    private static final int startupDelay = 1000;
    private String titleToType;

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
            Log.e("SignUpIntentTestSetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 02.07.01 - Sign up to attend an event
     * US 02.09.01 - Know signed up events
     * Creates a new test event, signs up for the event, returns to the event list, filters by
     * signed up events, and checks that the test event appears
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testSignUpAndFilter() throws InterruptedException {
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

        // Sign up for event
        onView(withId(R.id.btnSignUpEvent)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.sort_events_button)).perform(click());
        onView(withText("Signed Up")).perform(click());
        Thread.sleep(2000); // Wait for filtering to finish
        onView(withText(titleToType)).check(matches(isDisplayed()));

    }

    /**
     * Helper function to fill all the fields to create a new test event
     */
    private void createNewTestEvent() {
        // Set title
        titleToType = "SignUp intent test event " + (Math.random() * 10 + 1);
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
        String descToType = "SignUp intent test event";
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
            Log.e("SignUpIntentTestCleanUp", "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
