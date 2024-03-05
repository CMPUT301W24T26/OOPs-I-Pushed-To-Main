package com.example.qrscannertest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.content.Context;


/**
 * This class will hold a QR code.
 * It will contain the bitmap image as well as functions to generate and read a code
 */
public class QRCode {
    // Storing the string and bitmap of the qrcode
    private Bitmap qrImage;
    private String qrString;

    public Bitmap getQrImage() {
        return qrImage;
    }

    public void setQrImage(Bitmap qrImage) {
        this.qrImage = qrImage;
    }

    public String getQrString() {
        return qrString;
    }

    public void setQrString(String qrString) {
        this.qrString = qrString;
    }

    // ChatGPT, How would I use ZXing to generate a QR code?
    public void generateCode(String text) throws WriterException {
        // The size of the QR Code
        int qrSize = 400;

        // Create the MultiFormatWriter to generate the qr code
        MultiFormatWriter writer = new MultiFormatWriter();
        // Encode the text into a QR code format
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, qrSize, qrSize);
        // Create a new bitmap
        Bitmap bmp = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565);

        // Create the bitmap pixel by pixel
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        // Store the bitmap in the class
        qrImage = bmp;

    }

    public void scanCode(Context context) {
        // Switch to the scanning activity and scan
        Intent intent = new Intent(context, QRScanner.class);
        // Start the activity
        context.startActivity(intent);

    }

}
