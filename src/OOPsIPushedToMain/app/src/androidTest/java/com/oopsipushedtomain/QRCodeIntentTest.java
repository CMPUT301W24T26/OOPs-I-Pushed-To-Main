package com.oopsipushedtomain;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class QRCodeIntentTest {

    private User user;

    @Rule
    public ActivityTestRule<EventListActivity> eventListActivityTestRule =
            new ActivityTestRule<>(EventListActivity.class, true, true);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Test for US 01.01.01: Creating a new event with a unique QR code
     * Test for US 01.01.02: Reusing an existing QR code
     * Test for US 01.06.01: Sharing a generated QR code
     * Test for US 01.07.01: Generating a unique promotion QR code
     * @throws InterruptedException
     */
    @Test
    public void testViewEventQRCodeAndShare() throws InterruptedException {

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

    /**
     * Test for US 02.01.01: Checking into an event by scanning a QR code
     */
    @Test
    public void testCheckIntoEventByScanningQRCode() {
        // Before QRScanner is launched, prepare to mock its result.
        Intent resultData = new Intent();
        String mockQRCode = "mockEvent123"; // The mock QR code your scanner "scanned"
        resultData.putExtra("SCAN_RESULT", mockQRCode); // Use the same key that QRScanner uses to put the result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub the intent that QRScanner is expected to return
        intending(not(isInternal())).respondWith(result);

        // Now, trigger the QRScanner from your activity
        onView(withId(R.id.scanQRCodeButton)).perform(click());

        // Espresso intents will intercept and return the mocked result

        // After receiving the mock result, verify how your app handles the QR code.
        // This might involve verifying a view is updated, a network call is made, or an event is checked in.
        // Example: Verify an event detail activity is launched with the event ID from the QR code.
        intended(hasComponent(EventDetailsActivity.class.getName()));
    }

//    public static ViewAction waitForView(final Matcher<View> viewMatcher, final long timeout) {
//        return new ViewAction() {
//            @Override
//            public Matcher<View> getConstraints() {
//                return isRoot();
//            }
//
//            @Override
//            public String getDescription() {
//                return "wait up to " + timeout + " milliseconds for the view";
//            }

//            @Override
//            public void perform(UiController uiController, View view) {
//                long startTime = System.currentTimeMillis();
//                final long endTime = startTime + timeout;
//                final Matcher<View> conditionMatcher = allOf(viewMatcher, isDisplayed());
//
//                do {
//                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
//                        // Check if the condition is met
//                        if (conditionMatcher.matches(child)) {
//                            return;
//                        }
//                    }
//
//                    uiController.loopMainThreadForAtLeast(50);
//                    startTime = System.currentTimeMillis();
//                } while (startTime < endTime);
//
//                // View not displayed within the timeout
//                throw new PerformException.Builder()
//                        .withCause(new TimeoutException())
//                        .build();
//
//            }
//        };
//    }
//
//    // Utility method for adding delays
//    private static ViewAction waitFor(final long millis) {
//        return new ViewAction() {
//            @Override
//            public Matcher<View> getConstraints() {
//                return isRoot();
//            }
//
//            @Override
//            public String getDescription() {
//                return "wait for " + millis + " milliseconds";
//            }
//
//            @Override
//            public void perform(UiController uiController, View view) {
//                uiController.loopMainThreadForAtLeast(millis);
//            }
//        };
//    }
}

