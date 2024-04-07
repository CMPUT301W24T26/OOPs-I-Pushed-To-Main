package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.fail;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventListActivityTest {

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule = new ActivityScenarioRule<>(EventListActivity.class);

    @Rule
    public GrantPermissionRule permissionFineLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionCoarseLoc = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionNotifications = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    private User user;

    @Before
    public void setup() {
        activityRule.getScenario().onActivity(activity -> {
        });
    }

    /**
     * Test if the EventListActivity displays the list of events.
     */
    @Test
    public void testEventListDisplay() {
        onView(withId(R.id.EventListView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test the functionality of the sort button by verifying if the sort options menu is displayed.
     */
    @Test
    public void testSortButtonFunctionality() {
        onView(withId(R.id.sort_events_button)).perform(click());
        onView(withText("Sort by date")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test navigating to an event's details.
     * This test assumes that there is at least one event in the list.
     */
    @Test
    public void testNavigateToEventDetails() {
        // Click on the first event in the list
        onView(withId(R.id.EventListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check if the EventDetailsActivity is opened by verifying the presence of a specific view
        onView(withId(R.id.eventDetailsLayoutXMl)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @After
    public void cleanUp() {
        try {
            user.deleteUser().get();
        } catch (Exception e) {
            fail("Cleanup failed: " + e.getMessage());
        }
    }
}



