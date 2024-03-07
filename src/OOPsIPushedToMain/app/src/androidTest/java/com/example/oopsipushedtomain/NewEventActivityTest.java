package com.example.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NewEventActivityTest {
    @Rule
    public ActivityScenarioRule<NewEventActivity> activityRule =
            new ActivityScenarioRule<>(NewEventActivity.class);

    @Test
    public void testCreateNewEvent() {
        onView(withId(R.id.new_event_title_e)).perform(clearText(), typeText("Test Event"));
        onView(withId(R.id.new_event_start_time_e)).perform(clearText(), typeText("01/01/2025 12:00"));
        onView(withId(R.id.new_event_end_time_e)).perform(clearText(), typeText("01/01/2025 14:00"));
        onView(withId(R.id.new_event_description_e)).perform(clearText(), typeText("This is a test event."));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCreateNewEvent)).perform(click());


    }
}
