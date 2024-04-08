package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Events.ViewLimitAttendeesActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RunWith(AndroidJUnit4.class)
public class ViewLimitAttendeesActivityTest {

    /**
     * Launches {@link ViewLimitAttendeesActivity} before each test method.
     * <p>
     * This setup initializes the intent required to launch ViewLimitAttendeesActivity with necessary extras,
     * such as the eventId, to simulate the activity being launched in a real scenario.
     * </p>
     */
    @Rule
    public ActivityScenarioRule<ViewLimitAttendeesActivity> activityScenarioRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), ViewLimitAttendeesActivity.class)
                    .putExtra("eventId", "testEventId"));

    private FirebaseAccess mockFirebaseAccess;

    /**
     * Sets up the testing environment before each test.
     * <p>
     * This method initializes the mock object for FirebaseAccess and injects it into the activity
     * for testing purposes.
     * </p>
     */
    @Before
    public void setUp() {
        mockFirebaseAccess = Mockito.mock(FirebaseAccess.class);
        activityScenarioRule.getScenario().onActivity(activity -> {
            ViewLimitAttendeesActivity.setFirebaseAccessInstanceForTesting(mockFirebaseAccess);
        });
    }

    /**
     * Tests if the activity correctly fetches and displays the list of signed-up attendees.
     * <p>
     * This test simulates fetching signed-up attendees from Firestore and verifies that the ListView
     * is updated with the correct number of items.
     * </p>
     */
    @Test
    public void testFetchSignedUpAttendees() {
        Map<String, Object> fakeEventData = new HashMap<>();
        fakeEventData.put("signedUpAttendees", Arrays.asList("User1", "User2"));

        Mockito.when(mockFirebaseAccess.getDataFromFirestore("testEventId"))
                .thenReturn(CompletableFuture.completedFuture(fakeEventData));

        activityScenarioRule.getScenario().onActivity(ViewLimitAttendeesActivity::fetchSignedUpAttendees);

        onView(withId(R.id.signedUpAttendeesListView))
                .check(matches(hasChildCount(2)));
    }

    /**
     * Tests if the activity correctly updates the attendee limit in Firestore.
     * <p>
     * This test inputs a new attendee limit into the EditText field, simulates a click on the 'Set Limit'
     * button, and verifies that the correct update is made to Firestore.
     * </p>
     */
    @Test
    public void testSetAttendeeLimit() {
        onView(withId(R.id.limitEditText)).perform(typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.setLimitButton)).perform(click());

        activityScenarioRule.getScenario().onActivity(activity -> {
            Map<String, Object> expectedUpdate = new HashMap<>();
            expectedUpdate.put("attendeeLimit", 50);
            Mockito.verify(mockFirebaseAccess).storeDataInFirestore("testEventId", expectedUpdate);
        });
    }
}



