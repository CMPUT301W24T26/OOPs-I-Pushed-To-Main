package com.example.qrscannertest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.google.zxing.WriterException;

public class MainActivity extends AppCompatActivity {
    // Buttons and ImageViews
    Button scanButton, generateButton, showButton;
    ImageView qrImage;

    // QR code
    QRCode qrCode;

    // Activity result launcher for getting the result of the QRCodeScan
    private ActivityResultLauncher<Intent> qrCodeActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the three buttons on the screen
        scanButton = findViewById(R.id.scan_qr_code);
        generateButton = findViewById(R.id.generate_qr_code);
        showButton = findViewById(R.id.show_qr_code);

        // Get the image view
        qrImage = findViewById(R.id.qr_image);

        // Create an empty qr code
        qrCode = new QRCode();

        // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()
        // Register the activity result
        qrCodeActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    // Get the data
                    Intent data = o.getData();

                    // If the data is not null, retrieve and store in qrCode
                    if (data != null) {
                        String returnValue = data.getStringExtra("result");
                        qrCode.setQrString(returnValue);

                        // Show the scanned data
                        Toast.makeText(getApplicationContext(), qrCode.getQrString(), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Data Error", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Set on click listener for the generate button
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate a new code with the string "Testing"
                try {
                    qrCode.generateCode("Testing");
                } catch (WriterException e) {
                    // Catch the error, just print for now
                    e.printStackTrace();
                }

                // Print confirmation
                Toast.makeText(getApplicationContext(), "QR Generated", Toast.LENGTH_SHORT).show();
            }
        });

        // Set on click listener for the show button
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the QR Code on screen
                qrImage.setImageBitmap(qrCode.getQrImage());
            }
        });

        // Set the on click listener for the scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Switch to the scanning activity and scan
                Intent intent = new Intent(getApplicationContext(), QRScanner.class);

                // Start the activity
                qrCodeActivityResultLauncher.launch(intent);

                // This is asynchronous. DO NOT PUT CODE HERE
            }
        });


    }
}