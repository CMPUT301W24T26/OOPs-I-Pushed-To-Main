package com.oopsipushedtomain;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CheckInListIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess eventDB;
    private final int startupDelay = 1000;

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
     * Used to access the currently running EventDetailsActivity
     */
//    @Rule
//    public ActivityScenarioRule<EventDetailsActivity> eventDetailsActivityRule =
//            new ActivityScenarioRule<>(EventDetailsActivity.class);

    /**
     * Used to launch EventListActivity
     */
    @Rule(order = 1)
    public ActivityScenarioRule<EventListActivity> eventListActivityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * Initialize the databases.
     */
    @Before
    public void setup() {
        // Initialize the database
        try {
            eventDB = new FirebaseAccess(FirestoreAccessType.EVENTS);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("AnmtsIntentTestSetUp", "Error: " + e.getMessage());
            fail();
        }
    }

    /**
     * US 01.03.01 - Send notifications to attendees
     * US 02.03.01 - Receive push notifications from event organizers
     * US 02.04.01 - View events details and announcements
     * Creates a test event, signs up for the event, sends an announcement, checks that it appears
     * in the AnnouncementListActivity, and checks that the push notification was probably sent and
     * received.
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testAnnouncements() throws InterruptedException {
        Thread.sleep(startupDelay);
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
        String descToType = "Announcements test description " + (Math.random() * 10 + 1);
        onView(withId(R.id.new_event_description_e)).perform(ViewActions.typeText(descToType));
        Espresso.closeSoftKeyboard();
        // Limit attendees to 10
        onView(withId(R.id.new_event_attendee_limit_e)).perform(ViewActions.typeText("5"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());

        // Check-in to event
        Thread.sleep(startupDelay); // Wait for EventDetailsActivity to open
//        eventDetailsActivityRule.getScenario().onActivity(activity -> {
//            user = activity.getUser();
//            eventId = activity.getEventID();
//        });
        user.checkIn(eventId, getApplicationContext());

        // Verify that there is an attendee checked in
        Thread.sleep(2000);
        onView(withId(R.id.btnViewCheckedIn)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0)
                .onChildView(withId(R.id.checkInCount)).check(matches(withText("1")));

        // Check-in to event again
        user.checkIn(eventId, getApplicationContext());
        onView(withId(R.id.btnViewCheckedIn)).perform(click());

        // Verify that the count increased to 2
        Thread.sleep(10000);
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0)
                .onChildView(withId(R.id.checkInCount)).check(matches(withText("2")));

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
