package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.MatcherAssert.assertThat;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.oopsipushedtomain.Admin.AdminActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test admin activity
 */
@RunWith(AndroidJUnit4.class)
public class AdminActivityTest {

    /**
     * Create the activity
     */
    @Rule
    public ActivityTestRule<AdminActivity> activityRule =
            new ActivityTestRule<>(AdminActivity.class, true, true);

    /**
     * Test for browsing events
     */
    @Test
    public void testNavigateToBrowseEvents() {
        onView(withId(R.id.btnBrowseEvents)).perform(click());
        onView(withId(R.id.EventListView)).check(matches(isDisplayed()));
        //.check(new RecyclerViewNotEmptyAssertion());
    }

    /**
     * Test for browsing profiles
     */
    @Test
    public void testNavigateToBrowseProfiles() {
        onView(withId(R.id.btnBrowseProfiles)).perform(click());
        // Introduce a delay to wait for data to load
        try {
            Thread.sleep(2000); // wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.profilesRecyclerView)).check(matches(isDisplayed()))
                .check(new RecyclerViewNotEmptyAssertion());
    }

    /**
     * Test for browsing images and making a choice in ImageSelectionFragment
     */
    @Test
    public void testNavigateToBrowseImagesAndChoose() {
        onView(withId(R.id.btnBrowseImages)).perform(click());
        onView(withId(R.id.imageSelectionFragmentRoot)).check(matches(isDisplayed()));

        // Simulate choosing to browse event pictures
        onView(withId(R.id.btnProfilePictures)).perform(click());
        onView(withId(R.id.imagesRecyclerView)).check(matches(isDisplayed()))
                .check(new RecyclerViewNotEmptyAssertion());
    }

    /**
     * Test not empty
     */
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
