package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.fail;

import android.Manifest;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

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
public class ProfileActivityIntentTest {
    private User user;

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
     * US 02.02.03 - Update personal profile information (name)
     * US 02.05.01 - Automatic profile picture generation
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateName() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the name field to bring up the edit dialog
        onView(withId(R.id.nameTextView)).perform(click());
        typeTextInDialog("Mock name", 0);

        // Verify the name TextView is updated with the new name
        onView(withId(R.id.nameTextView)).check(matches(withText("Mock name")));
        
        // TODO pfp
    }

    /**
     * US 02.02.03 - Update personal profile information (nickname)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateNickName() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the nickname field to bring up the edit dialog
        onView(withId(R.id.nicknameTextView)).perform(click());
        typeTextInDialog("Mock nickname", 0);

        // Verify the nickname TextView is updated with the new nickname
        onView(withId(R.id.nicknameTextView)).check(matches(withText("Mock nickname")));
    }

    /**
     * US 02.02.03 - Update personal profile information (birthday)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateBirthday() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Get today's date as a string
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c);

        // Open the date picker and hit save (defaults to today's date)
        onView(withId(R.id.birthdayValueTextView)).perform(click());
        onView(withText("OK")).perform(click());

        // Verify the birthday TextView is updated with the new birthday
        onView(withId(R.id.birthdayValueTextView)).check(matches(withText(formattedDate)));
    }

    /**
     * US 02.02.03 - Update personal profile information (homepage URL)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateHomepage() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the homepage field to bring up the edit dialog
        onView(withId(R.id.homepageValueTextView)).perform(click());
        typeTextInDialog("www.mock-homepage.com", 0);

        // Verify the homepage TextView is updated with the new homepage URL
        onView(withId(R.id.homepageValueTextView)).check(matches(withText("www.mock-homepage.com")));
    }

    /**
     * US 02.02.03 - Update personal profile information (address)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateAddress() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the address field to bring up the edit dialog
        onView(withId(R.id.addressValueTextView)).perform(click());
        typeTextInDialog("123 Mock Street", 3);

        // Verify the address TextView is updated with the new address
        onView(withId(R.id.addressValueTextView)).check(matches(withText("123 Mock Street")));
    }

    /**
     * US 02.02.03 - Update personal profile information (phone number)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdatePhoneNumber() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the phone number field to bring up the edit dialog
        onView(withId(R.id.phoneNumberValueTextView)).perform(click());
        typeTextInDialog("1234567890", 0);

        // Verify the phone number TextView is updated with the new phone number
        onView(withId(R.id.phoneNumberValueTextView)).check(matches(withText("(123) 456-7890")));
    }

    /**
     * US 02.02.03 - Update personal profile information (email)
     *
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testUpdateEmail() throws InterruptedException {
        Thread.sleep(500); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> {
            user = activity.getUser();
        });

        // Click on the email field to bring up the edit dialog
        onView(withId(R.id.emailValueTextView)).perform(click());
        typeTextInDialog("mock-email@example.com", 0);

        // Verify the email TextView is updated with the new email
        onView(withId(R.id.emailValueTextView)).check(matches(withText("mock-email@example.com")));
    }

    /**
     * Helper function to type text into a dialog EditText that is already visible and press save.
     * @param textToType Text string that will be typed into the EditText.
     * @param numBackspaces Number of times to hit backspace before typing. Used for birthday since
     *                      its default value is "Bye".
     */
    public void typeTextInDialog(String textToType, int numBackspaces) {
        for (int i = 0; i < numBackspaces; i++) {
            onView(ViewMatchers.isAssignableFrom(EditText.class))
                    .perform(pressKey(KeyEvent.KEYCODE_DEL));
        }

        // Type the text, close the keyboard, and hit save
        onView(ViewMatchers.isAssignableFrom(EditText.class)).perform(ViewActions.typeText(textToType));
        Espresso.closeSoftKeyboard(); // Ensure the keyboard is closed before clicking the save button
        onView(withText("Save")).perform(click());
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
            Log.e("ProfileActivityIntentTest", "Clean-up error: " + e.getMessage());
            fail();
        }
    }
}
