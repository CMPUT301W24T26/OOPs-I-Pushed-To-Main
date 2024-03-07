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
public class EventDetailsActivityOrganizerTest {

    // Initialize with intent
    @Rule
    public ActivityScenarioRule<EventDetailsActivityOrganizer> activityRule =
            new ActivityScenarioRule<>(EventDetailsActivityOrganizer.class);

    @Test
    public void testEditEventDetails() {
        onView(withId(R.id.event_details_organizer_title_e)).perform(clearText(), typeText("Updated Test Event"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSaveEventDetails)).perform(click());


    }
}

