package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oopsipushedtomain.Events.EventListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the event list activity
 */
@RunWith(AndroidJUnit4.class)
public class EventListActivityTest {

    /**
     * Launches {@link EventListActivity} before each test method.
     */
    @Rule
    public ActivityScenarioRule<EventListActivity> activityScenarioRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * Verifies if the event list is displayed to the user.
     * <p>
     * This test checks whether the ListView that displays the list of events
     * is present and visible on the screen. It ensures that the main component
     * of the EventListActivity is functioning as expected.
     * </p>
     */
    @Test
    public void testEventListDisplay() {
        onView(withId(R.id.EventListView)).check(matches(isDisplayed()));
    }

    /**
     * Verifies the functionality of the sort button.
     * <p>
     * This test simulates a user clicking on the sort button and verifies if
     * the sort options menu is displayed. It ensures that the sort button
     * triggers the appropriate UI response.
     * </p>
     */
    @Test
    public void testSortButtonFunctionality() {
        onView(withId(R.id.sort_events_button)).perform(click());
        onView(withText("Sort by date")).check(matches(isDisplayed()));
    }

    // Additional tests can be added here to cover other functionalities
}



