package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.ListView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminActivityTest {

    @Rule
    public ActivityTestRule<AdminActivity> activityRule =
            new ActivityTestRule<>(AdminActivity.class, true, true);

    // Test for browsing events
    @Test
    public void testNavigateToEventListAndVerifyNotEmpty() throws InterruptedException {
        // Navigate to the EventListActivity by clicking a button
        onView(withId(R.id.btnBrowseEvents)).perform(click());

        // Wait for a few seconds to allow the list to populate
        Thread.sleep(3000);

        // Check if the ListView is populated
        onView(withId(R.id.EventListView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                ListView listView = (ListView) view;
                assertTrue("ListView is not populated", listView.getAdapter().getCount() > 0);
            }
        });
    }


    // Test for browsing profiles
    @Test
    public void testNavigateToBrowseProfiles() {
        onView(withId(R.id.btnBrowseProfiles)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.profilesRecyclerView)).check(matches(isDisplayed()))
                .check(new RecyclerViewNotEmptyAssertion());
    }

    // Test for browsing images and making a choice in ImageSelectionFragment
    @Test
    public void testNavigateToBrowseImagesAndChooseProfile() throws InterruptedException {
        onView(withId(R.id.btnBrowseImages)).perform(click());
        onView(withId(R.id.imageSelectionFragmentRoot)).check(matches(isDisplayed()));

        // Simulate choosing to browse event pictures
        onView(withId(R.id.btnProfilePictures)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.imagesRecyclerView)).check(matches(isDisplayed()))
                .check(new RecyclerViewNotEmptyAssertion());
    }

    @Test
    public void testNavigateToBrowseImagesAndChooseEvents() throws InterruptedException {
        onView(withId(R.id.btnBrowseImages)).perform(click());
        onView(withId(R.id.imageSelectionFragmentRoot)).check(matches(isDisplayed()));

        // Simulate choosing to browse event pictures
        onView(withId(R.id.btnEventPictures)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.imagesRecyclerView)).check(matches(isDisplayed()))
                .check(new RecyclerViewNotEmptyAssertion());
    }


    public class RecyclerViewNotEmptyAssertion implements ViewAssertion {
        @Override
        public void check(View view, NoMatchingViewException noViewException) {
            if (noViewException != null) {
                throw noViewException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            assertThat(adapter.getItemCount(), Matchers.greaterThan(0));
        }
    }
}
