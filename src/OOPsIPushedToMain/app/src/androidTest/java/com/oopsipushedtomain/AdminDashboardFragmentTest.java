package com.oopsipushedtomain;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.oopsipushedtomain.Admin.AdminDashboardFragment;

/**
 * Test admin dashboard
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AdminDashboardFragmentTest {

    /**
     * Set up the test
     */
    @Before
    public void setUp() {
        // Launch the fragment in the testing environment
        FragmentScenario.launchInContainer(AdminDashboardFragment.class, null);
    }

    /**
     * Click the browse button
     */
    @Test
    public void testBrowseEventsButtonClick() {
        // Perform a click on the browse events button
        onView(withId(R.id.btnBrowseEvents)).perform(click());

        // Verify the action, add appropriate assertions or mock behaviors
    }

    /**
     * Test browse profiles
     */
    @Test
    public void testBrowseProfilesButtonClick() {
        // Perform a click on the browse profiles button
        onView(withId(R.id.btnBrowseProfiles)).perform(click());

        // Verify the action, add appropriate assertions or mock behaviors
    }

    /**
     * Click browse images
     */
    @Test
    public void testBrowseImagesButtonClick() {
        // Perform a click on the browse images button
        onView(withId(R.id.btnBrowseImages)).perform(click());

        // Verify the action, add appropriate assertions or mock behaviors
    }
}
