package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Intent test for Announcements. Covers the following user stories:
 * US 01.03.01 - Send notifications to attendees.
 * US 02.03.01 - Receive push notifications from event organizers.
 * US 02.04.01 - View events details and announcements.
 *
 * @author Aidan Gironella
 * @see com.oopsipushedtomain.Announcements
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnnouncementsIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess eventDB;

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
            eventDB = new FirebaseAccess(FirestoreAccessType.EVENTS);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("AnmtsIntentTestSetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 01.03.01 - Send notifications to attendees.
     * US 02.03.01 - Receive push notifications from event organizers.
     * US 02.04.01 - View events details and announcements.
     * Creates a test event, signs up for the event, sends an announcement, checks that it appears
     * in the AnnouncementListActivity, and checks that the push notification was probably sent and
     * received.
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testAnnouncements() throws InterruptedException {
        int startupDelay = 1000;
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());
        onView(withId(R.id.eventsButton)).perform(click());

        // Create new test event
        // Set title
        String titleToType = "Announcements test event " + (Math.random() * 10 + 1);
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
        String descToType = "Announcements intent test event";
        onView(withId(R.id.new_event_description_e)).perform(ViewActions.typeText(descToType));
        Espresso.closeSoftKeyboard();
        // Limit attendees to 10
        onView(withId(R.id.new_event_attendee_limit_e)).perform(ViewActions.typeText("5"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());

        // Sign up for event
        onView(withId(R.id.btnSignUpEvent)).perform(click());

        // Intercept the event ID intent extra so that we can delete the event in cleanUp()
        Thread.sleep(2000);
        List<Intent> intents = Intents.getIntents();
        for (Intent intent : intents)
            // Get the extra data from the intent
            eventId = intent.getStringExtra("selectedEventId");

        // Release Intents
        Intents.release();

        // Send an announcement
        titleToType = "UI Test Notification " + (Math.random() * 10 + 1);
        String bodyToType = "This is a test notification";
        onView(withId(R.id.btnSendNotification)).perform(click());
        onView(withId(R.id.announcement_title_e)).perform(ViewActions.typeText(titleToType));
        onView(withId(R.id.announcement_body_e)).perform(ViewActions.typeText(bodyToType));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSendNotification)).perform(click());

        // Look for the announcement in the AnnouncementListActivity
        onView(withId(R.id.btnViewAnnouncements)).perform(click());
        Thread.sleep(3000);
        onView(withText(titleToType)).check(matches(isDisplayed()));

        // Wait for the push notification to arrive
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(titleToType)), 60000);
        UiObject2 title = device.findObject(By.text(titleToType));
        UiObject2 text = device.findObject(By.text(bodyToType));
        assertEquals(titleToType, title.getText());
        assertEquals(bodyToType, text.getText());
        device.pressBack();
    }

    /**
     * Clean up the database by deleting the test user, test event, and test announcement.
     */
    @After
    public void cleanUp() {
        // Delete the user
        try {
            user.deleteUser().get();
            eventDB.deleteDataFromFirestore(eventId).get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("ProfileActivityIntentTest", "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
