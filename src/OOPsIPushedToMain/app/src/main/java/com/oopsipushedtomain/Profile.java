/**
 * The Profile class represents a user profile in the event management system.
 * It contains personal information such as name, nickname, birthday, contact details, and more.
 */
package com.oopsipushedtomain;

import android.graphics.Bitmap;

/**
 * Represents an profile for the admin page
 */
public class Profile {
    /**
     * The unique identifier for the user
     */
    private String userId;
    /**
     * The full name of the user
     */
    private String name;
    /**
     * The nickname or alias of the user
     */
    private String nickname;
    /**
     * The birthday of the user (format: yyyy-MM-dd)
     */
    private String birthday;
    /**
     * The URL of the user's homepage or website
     */
    private String homepage;
    /**
     * The physical address of the user
     */
    private String address;
    /**
     * The phone number of the user
     */
    private String phone;
    /**
     * The UID of the user's profile picture
     */
    private String profileImageUID = null;
    /**
     * The user's profile picture
     */
    private Bitmap profileImage = null;
    /**
     * The email address of the user
     */
    private String email;

    /**
     * Default constructor for the Profile class.
     */
    public Profile() {}

    /**
     * Constructor to create a Profile object with specified details.
     * @param userId The unique identifier for the user.
     * @param name The full name of the user.
     * @param nickname The nickname or alias of the user.
     * @param birthday The birthday of the user (format: yyyy-MM-dd).
     * @param homepage The URL of the user's homepage or website.
     * @param address The physical address of the user.
     * @param phone The phone number of the user.
     * @param email The email address of the user.
     */
    public Profile(String userId, String name, String nickname, String birthday, String homepage, String address, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.birthday = birthday;
        this.homepage = homepage;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Retrieves the user's unique identifier.
     * @return The user's unique identifier.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user's unique identifier.
     * @param userId The user's unique identifier.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the user's full name.
     * @return The user's full name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's full name.
     * @param name The user's full name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the user's nickname or alias.
     * @return The user's nickname or alias.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the user's nickname or alias.
     * @param nickname The user's nickname or alias.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Retrieves the user's birthday.
     * @return The user's birthday (format: yyyy-MM-dd).
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * Sets the user's birthday.
     * @param birthday The user's birthday (format: yyyy-MM-dd).
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     * Retrieves the URL of the user's homepage or website.
     * @return The URL of the user's homepage or website.
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * Sets the URL of the user's homepage or website.
     * @param homepage The URL of the user's homepage or website.
     */
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * Retrieves the user's physical address.
     * @return The user's physical address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the user's physical address.
     * @param address The user's physical address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the user's phone number.
     * @return The user's phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     * @param phone The user's phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Retrieves the user's email address.
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email The user's email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
