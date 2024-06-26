package com.oopsipushedtomain.Admin;

import com.oopsipushedtomain.User.User;

/**
 * An interface for setting an on item click listener for the profile
 */
public interface OnItemClickListener {
    /**
     * When the profile is clicked
     * @param profile The prifile that was clicked on
     */
    void onItemClick(User profile);
}
