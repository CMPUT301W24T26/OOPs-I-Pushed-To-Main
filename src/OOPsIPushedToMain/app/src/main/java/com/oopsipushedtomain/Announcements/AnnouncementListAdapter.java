package com.oopsipushedtomain.Announcements;

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

import com.oopsipushedtomain.R;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter to handle a list of Announcement objects.
 * Inflates a custom item_announcement.xml layout to display the announcement info.
 * @author  Aidan Gironella
 * @see     Announcement
 * @see     AnnouncementListActivity
 */
public class AnnouncementListAdapter extends ArrayAdapter<Announcement> {
    /**
     * Array list to store the announcement to show
     */
    private final ArrayList<Announcement> announcements;

    /**
     * The context the adapter is called from
     */
    private final Context context;

    /**
     * Constructs a new AnnouncementListAdapter
     * @param context The current context, used to inflate the layout file.
     * @param announcements ArrayList of Announcements objects to add to the adapter.
     */
    public AnnouncementListAdapter(Context context, ArrayList<Announcement> announcements) {
        super(context, 0, announcements);
        this.announcements = announcements;
        this.context = context;
    }

    /**
     * Provides a view for the AdapterView
     * @param position Position of an individual announcement in the list
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
            view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        }

        Announcement announcement = announcements.get(position);

        // Initialize views
        TextView announcementTitle = view.findViewById(R.id.announcement_list_item_title);
        TextView announcementBody = view.findViewById(R.id.announcement_list_item_body);
        ImageView announcementImage = view.findViewById(R.id.announcement_image_view);

        // Set the UI elements
        announcementTitle.setText(announcement.getTitle());
        announcementBody.setText(announcement.getBody());
        announcementImage.setImageResource(context.getResources().getIdentifier(announcement.getImageId(), "drawable", context.getPackageName()));

        return view;
    }
}
