package com.oopsipushedtomain;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.oopsipushedtomain.ImageSelectionFragment;

/**
 * Tests image selection
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ImageSelectionFragmentTest {

    /**
     * Sets up the test
     */
    @Before
    public void setUp() {
        // Launch the ImageSelectionFragment in the testing environment
        FragmentScenario.launchInContainer(ImageSelectionFragment.class, null);
    }

    /**
     * Clicks the pictures button
     */
    @Test
    public void testEventPicturesButtonClick() {
        // Perform a click on the Event Pictures button
        onView(withId(R.id.btnEventPictures)).perform(click());

        // Assertions or verifications can be added here if needed
    }

    /**
     * Clicks the profile pictures button
     */
    @Test
    public void testProfilePicturesButtonClick() {
        // Perform a click on the Profile Pictures button
        onView(withId(R.id.btnProfilePictures)).perform(click());

        // Assertions or verifications can be added here if needed
    }
}
