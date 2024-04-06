package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnnouncementsIntentTest {
    /**
     * Manually request post notifications permissions in order to test them
     */
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

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
        // Launch EventListActivity
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventListActivity.class);
        ActivityScenario.launch(i).onActivity(activity -> {
        });

        // Open an event and sign up for it
        Thread.sleep(2000);  // Wait for EventList to populate
        onData(anything()).inAdapterView(withId(R.id.EventListView)).atPosition(0).perform(click());
        onView(withId(R.id.btnSignUpEvent)).perform(click());

        // Send an announcement
        String titleToType = "UI Test Notification " + (Math.random() * 10 + 1);
        String bodyToType = "This is a test notification";
        onView(withId(R.id.btnSendNotification)).perform(click());
        onView(withId(R.id.announcement_title_e)).perform(ViewActions.typeText(titleToType));
        onView(withId(R.id.announcement_body_e)).perform(ViewActions.typeText(bodyToType));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSendNotification)).perform(click());

        // Look for the announcement in the AnnouncementListActivity
        onView(withId(R.id.btnViewAnnouncements)).perform(click());
        Thread.sleep(2000);
        onView(withText(titleToType)).check(matches(isDisplayed()));

        // Wait for the push notification to arrive
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(titleToType)), TIMEOUT);
        UiObject2 title = device.findObject(By.text(titleToType));
        UiObject2 text = device.findObject(By.text(bodyToType));
        assertEquals(titleToType, title.getText());
        assertEquals(bodyToType, text.getText());
    }
}
