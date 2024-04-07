package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityIntentTest {
    private User user;
    private FirebaseAccess userDB;
    private final String TAG = "ProfileActivityIntentTest";

    /**
     * Manually request all permissions so that the dialogs don't appear
     */
    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Used to access the currently running ProfileActivity
     */
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    /**
     * Initialize the database.
     */
    @Before
    public void setup() {
        // Initialize the database
        try {
            userDB = new FirebaseAccess(FirestoreAccessType.USERS);
            User.createNewObject().thenAccept(newUser -> {
                user = newUser;
            });
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e(TAG, "Setup error: " + e.getMessage());
            fail();
        }
    }

    /**
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testProfileActivity() throws InterruptedException {
        // Launch MainActivity
        Intent i = new Intent(getInstrumentation().getTargetContext(), ProfileActivity.class);
        ActivityScenario.launch(i);
        Thread.sleep(2000);
        ActivityScenario<ProfileActivity> scenario = activityRule.getScenario();
        scenario.onActivity(activity -> {
            user = activity.getUser();

            // Get the automatically generated test user
            try {
                Thread.sleep(2000);  // Wait for automatic profile generation to finish
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//        ProfileActivity profileActivity = profileActivityRule.getActivity();
//        user = profileActivity.getUser();

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
            try {
                Thread.sleep(3000);  // Wait for EventList to populate
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            onView(withText(containsString(titleToType))).perform(click());
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
        });
    }

    /**
     * Clean up the database by deleting the test user, test event, and test announcement.
     */
    @After
    public void cleanUp() {
        // Delete the user
        try {
            user.deleteUser().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e(TAG, "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
