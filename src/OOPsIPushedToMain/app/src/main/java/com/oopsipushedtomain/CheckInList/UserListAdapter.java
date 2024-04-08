package com.oopsipushedtomain.CheckInList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.oopsipushedtomain.User;
import com.oopsipushedtomain.R;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter to handle a list of User objects.
 * Inflates a custom item_profile.xml layout to display the User info.
 * @author  Aidan Gironella
 * @see     User
 */
public class UserListAdapter extends ArrayAdapter<User> {
    /**
     * Array list to store the User to show
     */
    private final ArrayList<User> Users;

    /**
     * The context the adapter is called from
     */
    private final Context context;

    /**
     * Constructs a new UserListAdapter
     * @param context The current context, used to inflate the layout file.
     * @param Users ArrayList of User objects to add to the adapter.
     */
    public UserListAdapter(Context context, ArrayList<User> Users) {
        super(context, 0, Users);
        this.Users = Users;
        this.context = context;
    }

    /**
     * Provides a view for the AdapterView
     * @param position Position of an individual User in the list
     * @param convertView Old view to reuse if applicable
     * @param parent Parent view to attach this view to
     * @return View corresponding to the data at the specified position
     */
    @SuppressLint("DiscouragedApi")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        }

        User user = Users.get(position);

        // Initialize views
        TextView userName = view.findViewById(R.id.nameTextView);
        ImageView userImage = view.findViewById(R.id.profileImageView);

        // Set the UI elements (profile picture and name)
        user.getProfileImage().thenAccept(image -> {
            if (image != null){
                userImage.setImageBitmap(image);
            }
            user.getName().thenAccept(userName::setText);
        });

        return view;
    }
}
