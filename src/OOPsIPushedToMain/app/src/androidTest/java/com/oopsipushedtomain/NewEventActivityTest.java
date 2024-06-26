package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oopsipushedtomain.Events.NewEventActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NewEventActivityTest {

    /**
     * Launches {@link NewEventActivity} before each test method.
     * <p>
     * This setup initializes the intent required to launch NewEventActivity with necessary extras,
     * such as the userId, to simulate the activity being launched in a real scenario.
     * </p>
     */
    @Rule
    public ActivityScenarioRule<NewEventActivity> activityScenarioRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), NewEventActivity.class)
                    .putExtra("userId", "testUserId"));

    /**
     * Tests if the new event creation form is displayed and interactable.
     * <p>
     * This test checks whether the form fields for creating a new event are present and
     * verifies that input can be entered into these fields. It also simulates a click on the
     * 'create' button to ensure that the button is clickable.
     * </p>
     */
    @Test
    public void testCreateNewEventForm() {
        onView(withId(R.id.new_event_title_e)).perform(clearText(), typeText("Test Event"));
        onView(withId(R.id.new_event_start_time_e)).perform(clearText(), typeText("01/01/2025 12:00"));
        onView(withId(R.id.new_event_end_time_e)).perform(clearText(), typeText("01/01/2025 14:00"));
        onView(withId(R.id.new_event_description_e)).perform(clearText(), typeText("This is a test event."));
        onView(withId(R.id.new_event_attendee_limit_e)).perform(clearText(), typeText("50"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());
    }

    // Additional tests can be added here to cover other functionalities
}


