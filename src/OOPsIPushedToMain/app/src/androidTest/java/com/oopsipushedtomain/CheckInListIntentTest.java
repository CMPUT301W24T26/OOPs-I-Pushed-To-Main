package com.oopsipushedtomain;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

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
 * Intent test for the CheckInList. Covers the following user stories:
 * US 01.05.01 - Track real-time attendance and alerts for milestones
 *
 * @author Aidan Gironella
 * @see com.oopsipushedtomain.CheckInList.CheckInListActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CheckInListIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess db;
    private static final int startupDelay = 1000;
    private static final int liveUpdateDelay = 8000;
    private String titleToType;
    private String descToType;

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
            Log.e("CheckInListIntentTestSetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 01.05.01 - Track real-time attendance
     * Creates a new test event, opens the CheckInList and verifies that there are no check-ins
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testNoCheckIns() throws InterruptedException {
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

        // Open CheckInListActivity and verify that it is empty
        onView(withId(R.id.btnViewCheckedIn)).perform(click());
        Thread.sleep(liveUpdateDelay); // Give the map some time to load markers
        onView(withId(R.id.attendee_list)).check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)));
    }

    /**
     * US 01.05.01 - Track real-time attendance
     * Creates a new test event, opens the CheckInList, checks-in once, and verifies that there
     * is 1 check-in and that the user count is 1.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testOneCheckIn() throws InterruptedException {
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

        // Check in once, wait for live update, and verify that there is a child with count = 1
        user.checkIn(eventId, getApplicationContext());
        onView(withId(R.id.btnViewCheckedIn)).perform(click());
        Thread.sleep(liveUpdateDelay);  // Wait for DB to update
        onView(withId(R.id.attendee_list)).check(ViewAssertions.matches(ViewMatchers.hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.attendee_list))
                .atPosition(0)
                .onChildView(withId(R.id.checkInCount))
                .check(matches(withText("1")));
    }

    /**
     * US 01.05.01 - Track real-time attendance
     * Creates a new test event, opens the CheckInList, checks-in once, verifies that there is 1
     * check-in and that the user count is 1, then checks-in again and verifies that there is still
     * 1 check-in but that the user count is now 2.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testTwoCheckIns() throws InterruptedException {
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

        // Check in once, wait for live update, and verify that there is a child with count = 1
        user.checkIn(eventId, getApplicationContext());
        onView(withId(R.id.btnViewCheckedIn)).perform(click());
        Thread.sleep(liveUpdateDelay);  // Wait for DB to update
        onView(withId(R.id.attendee_list)).check(ViewAssertions.matches(ViewMatchers.hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.attendee_list))
                .atPosition(0)
                .onChildView(withId(R.id.checkInCount))
                .check(matches(withText("1")));

        // Check in again, wait for live update, and verify that the child's count increased to 2
        user.checkIn(eventId, getApplicationContext());
        Thread.sleep(liveUpdateDelay);  // Wait for DB to update
        onView(withId(R.id.attendee_list)).check(ViewAssertions.matches(ViewMatchers.hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.attendee_list))
                .atPosition(0)
                .onChildView(withId(R.id.checkInCount))
                .check(matches(withText("2")));
    }

    /**
     * US 01.05.01 - Real-time attendance alerts for milestones
     * Creates a test event, checks in to it 10 times, and waits for the milestone notification
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testMilestones() throws InterruptedException {
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

        // Check in ten times and wait for notification
        for (int i = 0; i < 10; i++) {
            user.checkIn(eventId, getApplicationContext());
            Thread.sleep(1000); // Wait for check-in to go through
        }

        // Wait for the push notification to arrive
        String notifTitle = "Milestone reached!";
        String notifBody = "Congratulations, your event " + titleToType + " has reached 10 check-ins!";
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(notifTitle)), 60000);
        UiObject2 title = device.findObject(By.text(notifTitle));
        UiObject2 text = device.findObject(By.text(notifBody));
        assertEquals(notifTitle, title.getText());
        assertEquals(notifBody, text.getText());
        device.pressBack();
    }

    /**
     * Helper function to fill all the fields to create a new test event
     */
    private void createNewTestEvent() {
        // Set title
        titleToType = "CheckInList test event " + (Math.random() * 10 + 1);
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
        descToType = "CheckInList intent test event";
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
            Log.e("CheckInListIntentTestCleanUp", "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
