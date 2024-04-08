package com.oopsipushedtomain;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QRCodeIntentTest {

    private User user;

    @Rule
    public ActivityTestRule<ProfileActivity> activityRule =
            new ActivityTestRule<>(ProfileActivity.class, true, false);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);
    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);


    /**
     * Test for US 02.01.01: Checking into an event by scanning a QR code
     */
    @Test
    public void testCheckIntoEventByScanningQRCode() {
        activityRule.launchActivity(new Intent());
        // Before QRScanner is launched, prepare to mock its result.
        Intent resultData = new Intent();
        String mockQRCode = "mockEvent123"; // The mock QR code your scanner "scanned"
        resultData.putExtra("SCAN_RESULT", mockQRCode); // Use the same key that QRScanner uses to put the result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub the intent that QRScanner is expected to return
        intending(not(isInternal())).respondWith(result);

        // Now, trigger the QRScanner from your activity
        onView(withId(R.id.scanQRCodeButton)).perform(click());

        // Check if the next activity is started
        intended(hasComponent(ProfileActivity.class.getName()));
    }

    /**
     * Test for US 01.01.01: Creating a new event with a unique QR code
     * Test for US 01.01.02: Reusing an existing QR code
     * Test for US 01.06.01: Sharing a generated QR code
     * Test for US 01.07.01: Generating a unique promotion QR code
     * @throws InterruptedException
     */
    @Test
    public void testViewEventQRCodeAndShare() throws InterruptedException {
        activityRule.launchActivity(new Intent());
        Thread.sleep(1000); // Wait for automatic profile generation to complete
        //activityRule.getScenario().onActivity(activity -> user = activity.getUser());

        onView(withId(R.id.eventsButton)).perform(click());
        Thread.sleep(500);

        // Click the first event. Replace with onData(...) if using a ListView or RecyclerViewActions for RecyclerView.
        onData(anything()).inAdapterView(withId(R.id.EventListView)).atPosition(0).perform(click());

        // Use a custom wait method or Espresso Idling Resources to wait for EventDetailsActivity
        //onView(isRoot()).perform(waitForView(withId(R.id.btnViewPromoQRCode), 5000));
        Thread.sleep(500);

        // Click on "Event Check-In Code" to view the QR Code for the selected event
        onView(withId(R.id.btnViewPromoQRCode)).perform(click());

        // Again, wait for the QR code to be fully displayed if needed
        //onView(isRoot()).perform(waitForView(withId(R.id.image_view), 5000));
        Thread.sleep(500);

        // Click on the "Share" button
        onView(withId(R.id.share_button)).perform(click());

        // Verify that an intent to share content has been sent
        intended(hasAction(Intent.ACTION_CHOOSER));
    }

}

