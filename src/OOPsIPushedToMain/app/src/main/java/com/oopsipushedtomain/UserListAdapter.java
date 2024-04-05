package com.oopsipushedtomain;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * UserListAdapter is an adapter class for populating a RecyclerView with User objects.
 * It binds the data to the RecyclerView and manages the ViewHolder.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    /**
     * List to hold user data
     */
    private List<User> userList;
    /**
     * The context the adapter was called from
     */
    private Context context;

    /**
     * The click listener for the list items
     */
    private OnItemClickListener listener;

    /**
     * Constructor for the user list adapter
     * @param context The context it was called from
     * @param userList The list of users to show
     * @param listener The listener to deal with clicking on an item
     */
    public UserListAdapter(Context context, List<User> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    /**
     * ViewHolder class for layout binding.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        /**
         * The view to show the user's name
         */
        public TextView nameTextView;

        /**
         * The constructor
         * @param itemView The view to show the item
         */
        public UserViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView); // Ensure your layout has a TextView with this ID

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(userList.get(position));
                    }
                }
            });
        }
    }

    /**
     * Inflates the layout and creates a new UserViewHolder
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return The UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    /**
     * When the view is bound, set the text in the view
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Initially set to loading or empty text
        holder.nameTextView.setText("Loading...");

        // Asynchronously get the name
        user.getName().thenAccept(name -> {
            // Ensure the ViewHolder hasn't been recycled to display a different item
            if (position == holder.getAdapterPosition()) {
                // Update the UI on the UI thread
                holder.nameTextView.post(() -> holder.nameTextView.setText(name));
            }
        }).exceptionally(e -> {
            // Handle any errors here
            Log.e("UserAdapter", "Error fetching user name", e);
            return null;
        });
    }


    /**
     * Gets the number of users in the list
     *
     * @return The number of users in the list
     */
    @Override
    public int getItemCount() {
        return userList.size(); // Return the size of the userList
    }
}
