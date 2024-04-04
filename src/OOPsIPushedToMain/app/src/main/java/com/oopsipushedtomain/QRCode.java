package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * Represents an QR code within the application.
 * This class is used to model QR codes, including their details such as string and the UIDs linking them to their image and
 * what they are linked to, start and end times.
 *
 * <p>
 * Outstanding issues:
 * - Need to implement a delete function to remove it from the database
 */
public class QRCode {
    // Storing the string and bitmap of the qrcode
    /**
     * The UID of the image associated with this QR code
     */
    private String imageUID;

    private String eventID;

    /**
     * The Bitmap image of this QR code
     */
    private Bitmap qrCodeImage = null;

    /**
     * A reference to the Firestore database
     */
    FirebaseAccess database;

    /**
     * Generates a new qr code for the given text. It will store it into the database
     *
     * @param text The text that is shown when scanning the qr code
     */
    private QRCode() {
        // Initialize the database
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);
    }


    /**
     * Loads QRCode from database
     * @param eventID
     * @param imageType
     * @return
     */
    public static CompletableFuture<QRCode> loadQRCodeObject(String eventID, ImageType imageType) {
        QRCode createdQRCode = new QRCode();
        CompletableFuture<QRCode> future = new CompletableFuture<>();

        // Set the UID of the user
        createdQRCode.eventID = eventID;

        // Update the user details
        createdQRCode.updateQRCodeFromDatabase(imageType).thenAccept(result -> {
            future.complete(createdQRCode);
        });

        return future;
    }

    /**
     * Generates a new QR Code
     * @param eventID
     * @param imageType
     */
    public static QRCode createNewQRCodeObject(String eventID, ImageType imageType) {
        QRCode createQRCode = new QRCode();

        FirebaseInnerCollection innerColl = FirebaseInnerCollection.valueOf(imageType.name());
        String imageUID = FirebaseAccess.generateNewUID(FirestoreAccessType.EVENTS, innerColl);
        createQRCode.setImageUID(imageUID);

        // Generating QRCode
        // The size of the QR Code
        int qrSize = 400;

        // Create the MultiFormatWriter to generate the qr code
        MultiFormatWriter writer = new MultiFormatWriter();
        // Encode the text into a QR code format
        BitMatrix matrix = null;
        try {
            matrix = writer.encode(imageUID, BarcodeFormat.QR_CODE, qrSize, qrSize);
        } catch (Exception e) {
            Log.d("QRCODE", "Error generating QRCode");
        }
        // Create a new bitmap
        Bitmap bmp = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565);

        // Create the bitmap pixel by pixel
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        // Store image in class
        createQRCode.setQrImage(bmp);

        // Store QRCode
        createQRCode.database.storeImageInFirestore(eventID, imageUID, imageType, bmp, null);

        return createQRCode;
    }


    /**
     * Sets image UID
     * @param imageUID
     */
    public void setImageUID(String imageUID) {
        this.imageUID = imageUID;
    }

    /**
     * Loads a QR code image from Firebase Storage
     *
     * @param listener The listener for determining when the data transfer is done
     */
    public Bitmap getQRCodeImage() {
        return qrCodeImage;
    }

    public CompletableFuture<Void> updateQRCodeFromDatabase(ImageType imageType) {
        // Create the future to return
        CompletableFuture<Void> future = new CompletableFuture<>();

        database.getAllRelatedImagesFromFirestore(this.eventID, imageType).thenAccept(dataList -> {
            this.qrCodeImage = (Bitmap) dataList.get(0).get("image");

            // Complete the future
            future.complete(null);
        });

        // Return the future
        return future;
    }

    /**
     * Loads the QR code image into the database
     *
     * @param qrCodeImage The image to upload
     */
    private void setQrImage(Bitmap qrCodeImage) {
        // Store the image in the object
        this.qrCodeImage = qrCodeImage;
    }

}
