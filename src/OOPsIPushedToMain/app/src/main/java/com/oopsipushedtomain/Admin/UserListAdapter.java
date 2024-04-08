package com.oopsipushedtomain.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oopsipushedtomain.R;

import java.util.List;

/**
 * UserListAdapter is an adapter class for populating a RecyclerView with Profile objects.
 * It binds the data to the RecyclerView and manages the ViewHolder.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    private List<UserListActivity.Profile> profileList; // Use Profile objects
    private Context context;
    private OnItemClickListener listener;

    public UserListAdapter(Context context, List<UserListActivity.Profile> profileList, OnItemClickListener listener) {
        this.context = context;
        this.profileList = profileList;
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

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(profileList.get(position));
                }
            });
        }
    }

    /**
     * Inflates the layout and creates a new UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserListActivity.Profile profile = profileList.get(position);
        holder.nameTextView.setText(profile.getName() != null ? profile.getName() : "Name not available");
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public void setProfileList(List<UserListActivity.Profile> profileList) {
        this.profileList = profileList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(UserListActivity.Profile profile); // Ensure Profile is correctly imported or referenced
    }

}
