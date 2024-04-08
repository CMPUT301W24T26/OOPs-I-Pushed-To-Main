package com.oopsipushedtomain.QRCode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


/**
 * Implements a QR code scanner activity to scan a QR code to check into an event
 */
public class QRScanner extends AppCompatActivity {

    /**
     * Initializes the scanner
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.qr_scanner);

        // Initialize the scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    // Override the result handler
    // ChatGPT, How do I scan a QR code using ZXing in android?
    // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()

    /**
     * Receives the result from the scanner and sends it back to the calling activity
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the scanning result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
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
                Log.d("QR Code", result.getContents());
                // Set the result of the activity to result ok
                setResult(Activity.RESULT_OK, returnIntent);
                // Close the activity
                finish();
            }
        }

    }
}