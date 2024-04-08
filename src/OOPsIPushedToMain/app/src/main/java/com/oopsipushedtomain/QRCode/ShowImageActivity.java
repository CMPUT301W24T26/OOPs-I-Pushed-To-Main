package com.oopsipushedtomain.QRCode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.oopsipushedtomain.R;

public class ShowImageActivity extends AppCompatActivity {

    private Uri imageUri; // Declare imageUri at the class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        ImageView imageView = findViewById(R.id.image_view);
        String uriString = getIntent().getStringExtra("qrCodeUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString); // Initialize imageUri
            imageView.setImageURI(imageUri);
        }

        Button shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> {
            if (imageUri != null) {
                try {
                    // Since the image is already saved and its URI is obtained, directly use this URI
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType(getContentResolver().getType(imageUri));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ShowImageActivity.this, "Failed to share image.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
