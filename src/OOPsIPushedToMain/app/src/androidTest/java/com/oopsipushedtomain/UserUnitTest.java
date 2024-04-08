package com.oopsipushedtomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.User.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

public class UserUnitTest {

    User user;
    Bitmap testImage;
    Context testContext;

    @Before
    public void setUp(){
        // Create a new user
        try {
            user = User.createNewObject().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("SetUp", "Error: " + e.getMessage());
            fail();
        }

        // Set the test image
        testContext = InstrumentationRegistry.getInstrumentation().getContext();
        testImage = BitmapFactory.decodeResource(testContext.getResources(), com.oopsipushedtomain.test.R.drawable.test_image);
    }

    @Test
    public void testGetSetAddress(){
        // Set the address of the user
        user.setAddress("Test Address");

        // Get the address
        String address = null;
        try {
            address = user.getAddress().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetAddress", "Error: " + e.getMessage());
            fail();
        }

        // Test the address was updated
        assertEquals("Test Address", address);
    }

    @Test
    public void testGetSetBirthday(){
        // Set the birthday of the user
        Date newBirthday = new Date();
        user.setBirthday(newBirthday);

        // Get the birthday
        Date birthday = null;
        try {
            birthday = user.getBirthday().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetBirthday", "Error: " + e.getMessage());
            fail();
        }

        // Test the address was updated
        assertEquals(newBirthday.toString(), birthday.toString());
    }

    @Test
    public void testGetSetEmail(){
        // Set the email of the user
        user.setEmail("test@testing.com");

        // Get the email
        String email = null;
        try {
            email = user.getEmail().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetEmail", "Error: " + e.getMessage());
            fail();
        }

        // Test the email was updated
        assertEquals("test@testing.com", email);
    }

    @Test
    public void testGetSetHomepage(){
        // Set the homepage of the user
        user.setHomepage("testing.com");

        // Get the homepage
        String homepage = null;
        try {
            homepage = user.getHomepage().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetHomepage", "Error: " + e.getMessage());
            fail();
        }

        // Test the homepage was updated
        assertEquals("testing.com", homepage);
    }

    @Test
    public void testGetSetName(){
        // Set the name of the user
        user.setName("Hello World");

        // Get the name
        String name = null;
        try {
            name = user.getName().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetName", "Error: " + e.getMessage());
            fail();
        }

        // Test the name was updated
        assertEquals("Hello World", name);
    }

    @Test
    public void testGetSetNickame(){
        // Set the nickname of the user
        user.setNickname("This is an easter egg");

        // Get the nickname
        String nickname = null;
        try {
            nickname = user.getNickname().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetNickname", "Error: " + e.getMessage());
            fail();
        }

        // Test the nickname was updated
        assertEquals("This is an easter egg", nickname);
    }

    @Test
    public void testGetSetPhone(){
        // Set the phone of the user
        user.setPhone("780-473-7373");

        // Get the phone
        String phone = null;
        try {
            phone = user.getPhone().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetPhone", "Error: " + e.getMessage());
            fail();
        }

        // Test the phone was updated
        assertEquals("780-473-7373", phone);
    }

    @Test
    public void testGetSetProfileImage(){
        // Set the image of the user
        user.setProfileImage(testImage);

        // Get the image
        Bitmap image = null;
        try {
            image = user.getProfileImage().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetProfileImage", "Error: " + e.getMessage());
            fail();
        }

        // Compare the bitmaps
        // ChatGPT: How do I compare bitmaps in Hava
        // Check for equality of dimensions
        if (testImage.getWidth() != image.getWidth() || testImage.getHeight() != image.getHeight()) {
            fail();
        }

        // Check each pixel
        for (int y = 0; y < testImage.getHeight(); y++) {
            for (int x = 0; x < testImage.getWidth(); x++) {
                if (testImage.getPixel(x, y) != image.getPixel(x, y)) {
                    fail();
                }
            }
        }

    }

    @Test
    public void testGetSetFID(){
        // Set the fid of the user
        user.setFID("This is a FID");

        // Get the fid
        String fid = null;
        try {
            fid = user.getFID().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestSetPID", "Error: " + e.getMessage());
            fail();
        }

        // Test the fid was updated
        assertEquals("This is a FID", fid);
    }

    @Test
    public void testDeleteProfileImage() {
        // Add a profile image
        user.setProfileImage(testImage);

        // Check if it was added
        assertNotNull(user.getProfileImageUID());

        // Delete the image
        user.deleteProfileImage();

        // Check if it was deleted
        try {
            assertNull(user.getProfileImageUID().get());
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestDeleteProfileImage", "Error: " + e.getMessage());
            fail();
        }

        // Delete again to see if it crashes
        user.deleteProfileImage();

    }

    @Test
    public void testCheckIn() {
        // Check into a fake event
        user.checkIn("FAKEEVENT", testContext);

        // Check if they were checked in
        FirebaseAccess database = new FirebaseAccess(FirestoreAccessType.USERS);
        Map<String, Object> data = null;
        try {
            // Wait for a bit
            Thread.sleep(2000);

            data = database.getDataFromFirestore(user.getUID(), FirebaseInnerCollection.checkedInEvents, "FAKEEVENT").get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestCheckIn", "Error: " + e.getMessage());
            fail();
        }

        // Check the data
        assertNotNull(data);
        // Get the count
        long count = (long) data.get("count");
        assertEquals(1, count);

        // Check in again
        user.checkIn("FAKEEVENT", testContext);

        // Check if they were checked in
        data = null;
        try {
            // Wait for a bit
            Thread.sleep(2000);

            data = database.getDataFromFirestore(user.getUID(), FirebaseInnerCollection.checkedInEvents, "FAKEEVENT").get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestCheckIn", "Error: " + e.getMessage());
            fail();
        }

        // Check the data
        assertNotNull(data);
        count = (long) data.get("count");
        assertEquals(2, count);
    }


    @After
    public void cleanUp(){
        // Delete the user
        try {
            user.deleteUser().get();
        } catch (Exception e) {
            // There was an error, the test failed
            Log.e("TestGetData", "Error: " + e.getMessage());
            fail();
        }
    }

}
