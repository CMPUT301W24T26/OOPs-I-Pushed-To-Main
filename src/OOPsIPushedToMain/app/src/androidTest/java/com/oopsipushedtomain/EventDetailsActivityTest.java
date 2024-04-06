package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    @Before
    public void setup() {
        // Create an intent with necessary extras
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("userId", "testUserId");
        intent.putExtra("selectedEventId", "testEventId");

        // Launch the activity with the intent
        ActivityScenario.launch(intent);
    }

    @Test
    public void testEditEventDetails() {
        // Ensure the activity is in the correct state
        onView(withId(R.id.event_details_organizer_title_e)).check(matches(isDisplayed()));

        // Perform text input and button clicks
        onView(withId(R.id.event_details_organizer_title_e)).perform(clearText(), typeText("Updated Test Event"));
        Espresso.closeSoftKeyboard(); // Close the keyboard to ensure the button is clickable
        onView(withId(R.id.btnSaveEventDetails)).perform(click());


    }

    // Additional tests can be added here to cover other functionalities like viewing attendees, sending notifications, etc.
}


