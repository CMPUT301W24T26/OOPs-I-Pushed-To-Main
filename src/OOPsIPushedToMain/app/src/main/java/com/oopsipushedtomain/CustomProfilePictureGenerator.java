package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class CustomProfilePictureGenerator {
    String name;
    String initial;

    public CustomProfilePictureGenerator(String name) {
        this.name = name;

        if (name != null && !name.isEmpty()) {
            this.initial = String.valueOf(name.charAt(0)).toUpperCase();
        }
    }

    private static final int[] COLORS = {
            Color.parseColor("#f44336"), // Red
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#9E9E9E"), // Grey
            Color.parseColor("#607D8B")  // Blue Grey
    };

    private void setAvatarInitial() {
        if (name != null && !name.isEmpty()) {
            this.initial = String.valueOf(name.charAt(0)).toUpperCase();
        }
    }

    public Bitmap createInitialBitmap() {
        int size = 100; // Diameter of the circle
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Select a random index for the color array
        int index = (int) (Math.random() * COLORS.length);
        int backgroundColor = COLORS[index];

        // Draw circle
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2, size / 2, size / 2, backgroundPaint);

        // Draw text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // Set the text color
        textPaint.setTextSize(50); // Set the text size
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // Calculate vertical center
        float centerY = (canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(initial, size / 2, centerY, textPaint);

        return bitmap;
    }
}
