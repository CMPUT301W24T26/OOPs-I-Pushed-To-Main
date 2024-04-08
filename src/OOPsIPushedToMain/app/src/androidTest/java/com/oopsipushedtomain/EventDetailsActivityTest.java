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
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oopsipushedtomain.Events.EventDetailsActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the event details activity
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    /**
     * Sets up the testing environment before each test.
     * <p>
     * This method initializes the intent required to launch EventDetailsActivity with necessary extras.
     * It then launches the activity in a test environment.
     * </p>
     */
    @Before
    public void setup() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("userId", "testUserId");
        intent.putExtra("selectedEventId", "testEventId");
        ActivityScenario.launch(intent);
    }

    /**
     * Tests if the event details can be edited successfully.
     * <p>
     * This test checks if the activity is in the correct state, performs text input into the event title field,
     * and simulates a click on the save button.
     * </p>
     */
    @Test
    public void testEditEventDetails() {
        onView(withId(R.id.event_details_organizer_title_e)).check(matches(isDisplayed()));
        onView(withId(R.id.event_details_organizer_title_e)).perform(clearText(), typeText("Updated Test Event"));
        Espresso.closeSoftKeyboard(); // Close the keyboard to ensure the button is clickable
        onView(withId(R.id.btnSaveEventDetails)).perform(click());
    }

    // Additional tests can be added here to cover other functionalities
}



