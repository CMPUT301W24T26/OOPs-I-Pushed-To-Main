package com.example.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.util.regex.Pattern.matches;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminDashboardFragmentTest {

    @Before
    public void setUp() {
        // Launch the fragment in a test environment
        FragmentScenario.launchInContainer(AdminDashboardFragment.class, null, R.style.AppTheme);
    }

    // Test methods will go here
    @Test
    public void testButtonVisibility() {
        onView(withId(R.id.btnBrowseEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBrowseProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBrowseImages)).check(matches(isDisplayed()));
    }

    @Test
    public void testButtonText() {
        onView(withId(R.id.btnBrowseEvents)).check(matches(withText("Browse Events")));
        onView(withId(R.id.btnBrowseProfiles)).check(matches(withText("Browse Profiles")));
        onView(withId(R.id.btnBrowseImages)).check(matches(withText("Browse Images")));
    }

}

