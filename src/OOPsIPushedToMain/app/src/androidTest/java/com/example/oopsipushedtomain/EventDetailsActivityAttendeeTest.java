package com.example.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityAttendeeTest {

    // Initialize with intent to pass a specific event ID or details
    @Rule
    public ActivityScenarioRule<EventDetailsActivityAttendee> activityRule =
            new ActivityScenarioRule<>(EventDetailsActivityAttendee.class);

    @Test
    public void testDisplayEventDetails() {

        onView(withId(R.id.event_details_attendee_title_a)).check(matches(withText("Test Event")));

    }
}

