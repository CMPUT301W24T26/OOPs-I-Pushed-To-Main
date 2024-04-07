package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.Manifest;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnnouncementsIntentTest {
    private User user;
    private String eventId;
    private FirebaseAccess eventDB;
    private FirebaseAccess anmtDB;
    private String anmtId;

    /**
     * Manually request fine location permissions so that the dialog doesn't pop up
     */
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    /**
     * Manually request coarse location permissions so that the dialog doesn't pop up
     */
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    /**
     * Manually request post notifications permissions in order to test them
     */
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Used to access the currently running EventDetailsActivity
     */
    @Rule
    public ActivityTestRule<EventDetailsActivity> eventDetailsActivityRule = new ActivityTestRule<>(EventDetailsActivity.class);

    /**
     * Used to access the currently running AnnouncementListActivity
     */
    @Rule
    public ActivityTestRule<AnnouncementListActivity> anmtListActivityRule = new ActivityTestRule<>(AnnouncementListActivity.class);

    /**
     * Used to access the currently running ProfileActivity
     */
    @Rule
    public ActivityTestRule<ProfileActivity> profileActivityRule = new ActivityTestRule<>(ProfileActivity.class);

    /**
     * Initialize the databases.
     */
    @Before
    public void setup() {
        // Initialize the databases
        try {
            eventDB = new FirebaseAccess(FirestoreAccessType.EVENTS);
            anmtDB = new FirebaseAccess(FirestoreAccessType.ANNOUNCEMENTS);
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
     * Sends an announcement, checks that it appears in the AnnouncementListActivity, and checks
     * that the push notification was probably sent and received.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testAnnouncements() throws InterruptedException {
        // Launch MainActivity
        Intent i = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        ActivityScenario.launch(i);

        // Open Events list and create a test event
        Thread.sleep(2000);  // Wait for automatic profile generation to finish
        ProfileActivity profileActivity = profileActivityRule.getActivity();
        user = profileActivity.getUser();
        user.setName("Announcements tester");
        onView(withId(R.id.eventsButton)).perform(click());
        String titleToType = "Announcements test event " + (Math.random() * 10 + 1);
        String startDateToType = "2024-01-01";
        String endDateToType = "2024-01-02";
        String descriptionToType = "Event to test announcements";
        onView(withId(R.id.create_event_button)).perform(click());
        onView(withId(R.id.new_event_title_e)).perform(ViewActions.typeText(titleToType));
        onView(withId(R.id.new_event_start_time_e)).perform(ViewActions.typeText(startDateToType));
        onView(withId(R.id.new_event_end_time_e)).perform(ViewActions.typeText(endDateToType));
        onView(withId(R.id.new_event_description_e)).perform(ViewActions.typeText(descriptionToType));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());

        // Open an event and sign up for it, thus signing up to receive push notifications
        Thread.sleep(3000);  // Wait for EventList to populate
        onView(withText(containsString(titleToType))).perform(click());
        EventDetailsActivity eventDetailsActivity = eventDetailsActivityRule.getActivity();
        eventId = eventDetailsActivity.getEventID();
        onView(withId(R.id.btnSignUpEvent)).perform(click());

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
        AnnouncementListActivity anmtListActivity = anmtListActivityRule.getActivity();
        anmtId = anmtListActivity.getFirstAnnouncement();

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
        // Delete the user, event, and announcement
        try {
            user.deleteUser().get();
            eventDB.deleteDataFromFirestore(eventId);
            anmtDB.deleteDataFromFirestore(anmtId);
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("AnmtsIntentTestCleanUp", "Error: " + e.getMessage());
            fail();
        }
    }
}
