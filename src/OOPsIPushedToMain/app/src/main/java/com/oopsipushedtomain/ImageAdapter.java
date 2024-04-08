package com.oopsipushedtomain;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter class for managing a RecyclerView that displays a list of images.
 * This class is responsible for providing the views that represent each item in the image list.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    /**
     * The list to store the info for each image
     */
    private List<ImageInfo> imageInfos;
    /**
     * The context the adapter was called from
     */
    private Context context;
    /**
     * The click listener for the item in the list
     */
    private OnItemClickListener listener;

    /**
     * Interface definition for a callback to be invoked when an item in this adapter has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item has been clicked.
         *
         * @param position The position of the item in the adapter that was clicked.
         */
        void onItemClick(int position);
    }

    /**
     * Constructs a new ImageAdapter.
     *
     * @param context    The current context.
     * @param imageInfos A list of ImageInfo objects to be displayed.
     * @param listener   A listener to respond to item click events.
     */
    public ImageAdapter(Context context, List<ImageInfo> imageInfos, OnItemClickListener listener) {
        this.context = context;
        this.imageInfos = imageInfos;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view, listener);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        ImageInfo imageInfo = imageInfos.get(position);
        holder.imageView.setImageBitmap(imageInfo.getImage());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return imageInfos.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The view for the image
         */
        ImageView imageView;

        /**
         * Constructs a new ViewHolder instance.
         *
         * @param itemView The view of the RecyclerView item.
         * @param listener The click listener for this item.
         */
        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
