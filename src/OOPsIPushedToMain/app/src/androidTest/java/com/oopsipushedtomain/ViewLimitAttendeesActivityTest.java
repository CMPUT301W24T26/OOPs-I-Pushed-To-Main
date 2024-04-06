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
import androidx.test.rule.ActivityTestRule;

import com.oopsipushedtomain.Database.FirebaseAccess;

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

    @Rule
    public ActivityScenarioRule<ViewLimitAttendeesActivity> activityScenarioRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), ViewLimitAttendeesActivity.class).putExtra("eventId", "testEventId"));

    private FirebaseAccess mockFirebaseAccess;

    @Before
    public void setUp() {

        mockFirebaseAccess = Mockito.mock(FirebaseAccess.class);

        activityScenarioRule.getScenario().onActivity(activity -> {
            ViewLimitAttendeesActivity.setFirebaseAccessInstanceForTesting(mockFirebaseAccess);
        });
    }

    @Test
    public void testFetchSignedUpAttendees() {
        // Prepare the data to be returned by the mock
        Map<String, Object> fakeEventData = new HashMap<>();
        fakeEventData.put("signedUpAttendees", Arrays.asList("User1", "User2"));
        
        Mockito.when(mockFirebaseAccess.getDataFromFirestore("testEventId"))
                .thenReturn(CompletableFuture.completedFuture(fakeEventData));

        activityScenarioRule.getScenario().onActivity(ViewLimitAttendeesActivity::fetchSignedUpAttendees);

        onView(withId(R.id.signedUpAttendeesListView))
                .check(matches(hasChildCount(2)));
    }

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


