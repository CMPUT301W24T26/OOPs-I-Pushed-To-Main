package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventListActivityTest {

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    @Test
    public void testEventListDisplay() {
        // Check if the event list is displayed
        onView(withId(R.id.EventListView)).check(matches(isDisplayed()));
    }



}


