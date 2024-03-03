package com.example.oopsipushedtomain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class QRScanner extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner);

        // Initialize the scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    // Override the result handler
    // ChatGPT, How do I scan a QR code using ZXing in android?
    // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the scanning result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                // Canceled
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();

                // Close the activity without returning a value
                setResult(Activity.RESULT_CANCELED);
                finish();

            } else {
                // Scan successful
                // Return the result
                Intent returnIntent = new Intent();
                // Put the string as an extra
                returnIntent.putExtra("result", result.getContents());
                // Set the result of the activity to result ok
                setResult(Activity.RESULT_OK, returnIntent);
                // Close the activity
                finish();
            }
        }

    }
}