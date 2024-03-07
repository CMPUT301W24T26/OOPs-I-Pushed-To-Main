package com.example.oopsipushedtomain;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AttendeeProfileActivityTest {

    @Rule
    public ActivityScenarioRule<AttendeeProfileActivity> activityRule =
            new ActivityScenarioRule<>(AttendeeProfileActivity.class);

    @Test
    public void testUpdateName() {
        // Simulate a user clicking on the name field to bring up the edit dialog
        onView(withId(R.id.nameTextView)).perform(click());

        // Clear any existing text in the dialog's EditText, then type the new name
        onView(withId(R.id.editTextFieldValue))
                .perform(clearText(), typeText("New Name"));
        Espresso.closeSoftKeyboard(); // Ensure the keyboard is closed before clicking the save button

        // Click the "Save" button in the dialog to save the new name
        onView(withText("Save")).perform(click());

        // Verify the name TextView is updated with the new name
        onView(withId(R.id.nameTextView)).check(matches(withText("New Name")));
    }

    @Test
    public void testUpdateNickname() {
        onView(withId(R.id.nicknameTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("New Nickname"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.nicknameTextView)).check(matches(withText("New Nickname")));
    }

    @Test
    public void testUpdateBirthday() {
        onView(withId(R.id.birthdayValueTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("01/01/2000"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.birthdayValueTextView)).check(matches(withText("01/01/2000")));
    }

    @Test
    public void testUpdateAddress() {
        onView(withId(R.id.addressValueTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("123 New Address"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.addressValueTextView)).check(matches(withText("123 New Address")));
    }

    @Test
    public void testUpdateHomepage() {
        onView(withId(R.id.homepageValueTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("https://newhomepage.com"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.homepageValueTextView)).check(matches(withText("https://newhomepage.com")));
    }

    @Test
    public void testUpdatePhoneNumber() {
        onView(withId(R.id.phoneNumberValueTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("9876543210"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.phoneNumberValueTextView)).check(matches(withText("9876543210")));
    }

    @Test
    public void testUpdateEmail() {
        onView(withId(R.id.emailValueTextView)).perform(click());
        onView(withId(R.id.editTextFieldValue)).perform(clearText(), typeText("newemail@example.com"));
        Espresso.closeSoftKeyboard();
        onView(withText("Save")).perform(click());
        onView(withId(R.id.emailValueTextView)).check(matches(withText("newemail@example.com")));
    }
}
