package com.oopsipushedtomain.Events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.oopsipushedtomain.R;

import java.util.ArrayList;

/**
 * EventListAdapter is a custom ArrayAdapter designed to bind an ArrayList of Event objects
 * to views for display in a ListView. This adapter is responsible for creating view items
 * for each event in the list, allowing for the display of event titles (or other event properties)
 * within the ListView in EventListActivity.
 * <p>
 * This adapter inflates a custom layout (item_event.xml) for each item in the list, which currently
 * displays the event title. The adapter can be extended to display more detailed information about
 * each event as needed.
 * <p>
 * Outstanding issues:
 * 1. The adapter currently only displays the event title. There may be a need to display more information
 * about each event, such as the event date, location, or a thumbnail image, which would require modifications
 * to both the adapter's getView method and the item_event.xml layout.
 */

public class EventListAdapter extends ArrayAdapter<Event> {
    /**
     * An array list of events to show
     */
    private ArrayList<Event> events;

    /**
     * The context the adapter was called from
     */
    private Context context;

    /**
     * Constructs a new EventListAdapter.
     *
     * @param events  An ArrayList of Event objects to be represented in the ListView.
     * @param context The current context. Used to inflate the layout file.
     */
    public EventListAdapter(ArrayList<Event> events, Context context) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     * Provides a view for an AdapterView (ListView).
     *
     * @param position    The position in the list of data that should be displayed in the list item view.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the view
        convertView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);

        // Get the position in the list
        Event event = events.get(position);

        // Get the title for each event
        TextView titleName = convertView.findViewById(R.id.event_list_title);

        // Set the title of each event
        titleName.setText(event.getTitle());

        // Return the view
        return convertView;
    }
}
