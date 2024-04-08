package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
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
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.After;
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
     * Used to access the currently running ProfileActivity
     */
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    /**
     * US 02.06.01 - No login requirement for app access
     * US 02.02.03 - Update personal profile information (name)
     * US 02.05.01 - Automatic profile picture generation
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateName() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the name field to bring up the edit dialog
        onView(withId(R.id.nameTextView)).perform(click());
        typeTextInDialog("Mock name", 0);

        // Verify the name TextView is updated with the new name
        onView(withId(R.id.nameTextView)).check(matches(withText("Mock name")));

        // Verify automatic profile picture generation
        onView(withId(R.id.profileImageView)).check((profileImageView, noViewFoundException) -> {
            ImageView imageView = (ImageView) profileImageView;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            // Check if the bitmap is null
            assertNotNull(bitmap);
        });
    }

    /**
     * US 02.02.03 - Update personal profile information (nickname)
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateNickName() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the nickname field to bring up the edit dialog
        onView(withId(R.id.nicknameTextView)).perform(click());
        typeTextInDialog("Mock nickname", 0);

        // Verify the nickname TextView is updated with the new nickname
        onView(withId(R.id.nicknameTextView)).check(matches(withText("Mock nickname")));
    }

    /**
     * US 02.02.03 - Update personal profile information (birthday)
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateBirthday() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

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
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateHomepage() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the homepage field to bring up the edit dialog
        onView(withId(R.id.homepageValueTextView)).perform(click());
        typeTextInDialog("www.mock-homepage.com", 0);

        // Verify the homepage TextView is updated with the new homepage URL
        onView(withId(R.id.homepageValueTextView)).check(matches(withText("www.mock-homepage.com")));
    }

    /**
     * US 02.02.03 - Update personal profile information (address)
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateAddress() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the address field to bring up the edit dialog
        onView(withId(R.id.addressValueTextView)).perform(click());
        typeTextInDialog("123 Mock Street", 3);

        // Verify the address TextView is updated with the new address
        onView(withId(R.id.addressValueTextView)).check(matches(withText("123 Mock Street")));
    }

    /**
     * US 02.02.03 - Update personal profile information (phone number)
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdatePhoneNumber() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the phone number field to bring up the edit dialog
        onView(withId(R.id.phoneNumberValueTextView)).perform(click());
        typeTextInDialog("1234567890", 0);

        // Verify the phone number TextView is updated with the new phone number
        onView(withId(R.id.phoneNumberValueTextView)).check(matches(withText("(123) 456-7890")));
    }

    /**
     * US 02.02.03 - Update personal profile information (email)
     *
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testUpdateEmail() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the email field to bring up the edit dialog
        onView(withId(R.id.emailValueTextView)).perform(click());
        typeTextInDialog("mock-email@example.com", 0);

        // Verify the email TextView is updated with the new email
        onView(withId(R.id.emailValueTextView)).check(matches(withText("mock-email@example.com")));
    }

    /**
     * US 02.02.01 - Upload profile picture
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     * @throws UiObjectNotFoundException Used to find shutter button and confirm photo button
     */
    @Test
    public void testUploadProfilePicture() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Click on the profile picture
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withText("Take Photo")).perform(click());

        // Switch to UI Automator
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        // Wait for camera app to appear
        device.wait(Until.hasObject(By.pkg("com.android.camera2")), 5000);

        // Capture photo and confirm it
        UiObject takePhotoButton = device.findObject(new UiSelector().resourceId("com.android.camera2:id/shutter_button"));
        takePhotoButton.click();
        device.findObject(new UiSelector().resourceId("com.android.camera2:id/done_button")).click();

        // Check that the profile picture was saved
        onView(withId(R.id.profileImageView)).check((profileImageView, noViewFoundException) -> {
            ImageView imageView = (ImageView) profileImageView;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            // Verify that the bitmap exists
            assertNotNull(bitmap);
        });
    }

    /**
     * US 02.02.02 - Option to remove profile pictures
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     * @throws UiObjectNotFoundException Used to find shutter button and confirm photo button
     */
    @Test
    public void testDeleteProfilePicture() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        // Take and upload a profile picture
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withText("Take Photo")).perform(click());

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.wait(Until.hasObject(By.pkg("com.android.camera2")), 5000);
        UiObject takePhotoButton = device.findObject(new UiSelector().resourceId("com.android.camera2:id/shutter_button"));
        takePhotoButton.click();
        device.findObject(new UiSelector().resourceId("com.android.camera2:id/done_button")).click();

        // Delete profile picture
        Thread.sleep(2000); // Wait for camera to close and profile picture to upload
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withText("Delete Photo")).perform(click());

        // Check that the profile picture was deleted
        Thread.sleep(2000); // Wait for profile picture to be deleted
        onView(withId(R.id.profileImageView)).check((profileImageView, noViewFoundException) -> {
            ImageView imageView = (ImageView) profileImageView;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            // Verify that the bitmap is null
            assertNull(bitmap);
        });
    }

    /**
     * Tests that when a profile is initially created, there is no profile picture
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    @Test
    public void testEmptyPfp() throws InterruptedException {
        Thread.sleep(startupDelay); // Wait for automatic profile generation to complete
        activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        onView(withId(R.id.profileImageView)).check((profileImageView, noViewFoundException) -> {
            ImageView imageView = (ImageView) profileImageView;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            // Verify that the bitmap does not exist
            assertNull(drawable);
        });
    }

    /**
     * Helper function to type text into a dialog EditText that is already visible and press save.
     * @param textToType Text string that will be typed into the EditText.
     * @param numBackspaces Number of times to hit backspace before typing. Used for birthday since
     *                      its default value is "Bye".
     * @throws InterruptedException For Thread.sleep() and typeTextInDialog()
     */
    public void typeTextInDialog(String textToType, int numBackspaces) throws InterruptedException {
        for (int i = 0; i < numBackspaces; i++) {
            onView(ViewMatchers.isAssignableFrom(EditText.class))
                    .perform(pressKey(KeyEvent.KEYCODE_DEL));
        }

        // Type the text, close the keyboard, and hit save
        onView(ViewMatchers.isAssignableFrom(EditText.class)).perform(ViewActions.typeText(textToType));
        Espresso.closeSoftKeyboard(); // Ensure the keyboard is closed before clicking the save button
        Thread.sleep(1000); // Wait for keyboard to close
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
