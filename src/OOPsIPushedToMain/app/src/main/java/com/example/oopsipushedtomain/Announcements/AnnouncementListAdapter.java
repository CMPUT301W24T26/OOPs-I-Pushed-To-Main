package com.example.oopsipushedtomain.Announcements;

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

import com.example.oopsipushedtomain.R;

import java.util.ArrayList;

public class AnnouncementListAdapter extends ArrayAdapter<Announcement> {
    private final ArrayList<Announcement> announcements;
    private final Context context;

    public AnnouncementListAdapter(Context context, ArrayList<Announcement> announcements) {
        super(context, 0, announcements);
        this.announcements = announcements;
        this.context = context;
    }

    @SuppressLint("DiscouragedApi")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        }

        Announcement announcement = announcements.get(position);

        TextView announcementTitle = view.findViewById(R.id.announcement_list_item_title);
        TextView announcementBody = view.findViewById(R.id.announcement_list_item_body);
        ImageView announcementImage = view.findViewById(R.id.announcement_image_view);

        announcementTitle.setText(announcement.getTitle());
        announcementBody.setText(announcement.getBody());
        announcementImage.setImageResource(context.getResources().getIdentifier(announcement.getImageId(), "drawable", context.getPackageName()));

        return view;
    }
}
