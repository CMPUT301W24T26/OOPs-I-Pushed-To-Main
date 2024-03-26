package com.oopsipushedtomain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ProfileListAdapter is an adapter class for populating a RecyclerView with Profile objects.
 * It binds the data to the RecyclerView and manages the ViewHolder.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ProfileViewHolder> {
    /**
     * List to hold profile data
     */
    private List<User> profileList;
    /**
     * The context the adapter was called from
     */
    private Context context;

    /**
     * The click listener for the list items
     */
    private OnItemClickListener listener;

    /**
     * Constructor for the profile list adapter
     * @param context The context it was called from
     * @param profileList The list of profiles to show
     * @param listener The listener to deal with clicking on an item
     */
    public UserListAdapter(Context context, List<User> profileList, OnItemClickListener listener) {
        this.context = context;
        this.profileList = profileList;
        this.listener = listener;
    }

    /**
     * ViewHolder class for layout binding.
     */
    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        // Assuming you have a TextView called textViewName as part of your item layout
        /**
         * The view to show the item name
         */
        public TextView nameTextView;

        /**
         * The constructor
         * @param itemView The view to show the item
         */
        public ProfileViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(profileList.get(position));
                    }
                }
            });
        }
    }

    /**
     * Inflates the layout and creates a new ProfileViewHolder
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return The ProfileViewHolder
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the ViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(itemView);
    }

    /**
     * When the view is bound, set the text in the view
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User profile = profileList.get(position);
        holder.nameTextView.setText(profile.getName()); // Set name to TextView
        // Set other details to views as needed
    }

    /**
     * Gets the number of profiles in the list
     *
     * @return The number of profiles in the list
     */
    @Override
    public int getItemCount() {
        return profileList.size(); // Return the size of the profileList
    }
}
