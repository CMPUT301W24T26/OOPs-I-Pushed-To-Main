package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import java.io.ByteArrayOutputStream;
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
     * The string associated with this QR Code
     */
    private String qrString;
    /**
     * The UID of this QR Code
     */
    private String qrCodeUID;

    /**
     * The UID of the image associated with this QR code
     */
    private String imageUID;

    /**
     * The Bitmap image of this QR code
     */
    private Bitmap qrCodeImage = null;

    /**
     * A reference to the Firestore database
     */
    FirebaseAccess database;


    /**
     * Interface for checking when data is loaded into the qrcode
     */
    public interface DataLoadedListener {
        /**
         * Callback for when data is loaded
         */
        void onDataLoaded();
    }

    /**
     * Interface for checking when the image is loaded from the database
     */
    public interface OnBitmapReceivedListener {
        /**
         * Call back to pass the received Bitmap back to the calling function
         * @param bitmap The bitmap to pass
         */
        void onBitmapReceived(Bitmap bitmap);
    }

    /**
     * Interface for checking when there is a new code loaded into the class
     */
    public interface NewCodeListener {
        /**
         * Callback for when the qr code data is recieved
         * Now the image can be fetched
         */
        void onDataInitialized();
    }

    /**
     * Generates a new qr code for the given text. It will store it into the database
     *
     * @param text The text that is shown when scanning the qr code
     */
    public QRCode(String text) {
        // Initialize the database
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Generate the code
        try {
            generateCode(text);
        } catch (WriterException e) {
            Log.d("QR Code", "Generating Failed");
        }

    }

    /**
     * Loads a previously created QR code from the database
     *
     * @param qrCodeID The UID of the QR code to load
     * @param listener A listener for checking when the data is done loading
     */
    public QRCode(String qrCodeID, DataLoadedListener listener) {
        // Initialize database
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Set the document ID
        qrCodeUID = qrCodeID;
        database.getImageFromFirestore(qrCodeUID, ImageType.eventQRCodes);

        // Set the fields of the class
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateAllDataFields(new NewCodeListener() {
                    @Override
                    public void onDataInitialized() {
                        // Generate a new code
                        Log.d("QR Code", "Data init");
                        loadQrCodeImage(new OnBitmapReceivedListener() {
                            @Override
                            public void onBitmapReceived(Bitmap bitmap) {
                                if (bitmap != null) {
                                    qrCodeImage = bitmap;
                                }
                                listener.onDataLoaded();
                            }
                        });


                    }
                });

            }
        }).start();

    }

    /**
     * Updates all of the fields of the QRCode class from the database
     * It will also move on to load the actual QR image
     *
     * @param listener The listener to determine when data is received
     */
//    public void UpdateAllDataFields(QRCode.NewCodeListener listener) {
//        // Get the data in the document
//        qrCodeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // Get the data from the query - there should only be 1 document
//                        Map<String, Object> data = document.getData();
//
//                        // Get Single element fields
//                        assert data != null;
//                        qrString = (String) data.get("qrString");
//                        imageUID = (String) data.get("qrImage");
//
//                        assert imageUID != null;
//                        Log.d("QR Code", imageUID);
//
//                        // Inform complete
//                        Log.d("Firebase", "Data Loaded");
//                        listener.onDataInitialized();
//
//                    } else {
//                        Log.d("Firebase", "No such document");
//                    }
//                } else {
//                    Log.d("Firebase Failure", "get failed with ", task.getException());
//                }
//            }
//        });
//    }


    /**
     * Loads a QR code image from Firebase Storage
     *
     * @param listener The listener for determining when the data transfer is done
     */
    public void loadQrCodeImage(QRCode.OnBitmapReceivedListener listener) {
        if (qrCodeImage != null) {
            // If the image is already loaded, use it directly.
            listener.onBitmapReceived(qrCodeImage);
            return;
        }

        // Initialize FirebaseAccess for QR Codes
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Asynchronously get the image from Firestore
        CompletableFuture<Map<String, Object>> future = database.getImageFromFirestore(imageUID, ImageType.eventQRCodes);

        future.thenAccept(data -> {
            if (data != null) {
                // Image was successfully retrieved, so get the bitmap, set it to the class variable, and call the listener
                qrCodeImage = (Bitmap) data.get("image");
                Log.d("QR Code", "Image successfully loaded");
                listener.onBitmapReceived(qrCodeImage);
            } else {
                // Data was null, so the image wasn't found
                Log.d("QR Code", "Image failed to download");
                listener.onBitmapReceived(null);
            }
        }).exceptionally(e -> {
            // Handle any exceptions that occurred during the retrieval
            Log.d("QR Code", "Image failed to download", e);
            listener.onBitmapReceived(null);
            return null;
        });
    }

    /**
     * Stores the QR code string in the database
     *
     * @param qrString The string to store
     */
//    private void setQrString(String qrString) {
//        // Update in the class
//        this.qrString = qrString;
//
//        // Update in database
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("qrString", this.qrString);
//        qrCodeDocRef.update(data);
//    }


    /**
     * Loads the QR code image into the database
     *
     * @param qrCodeImage The image to upload
     */
//    private void setQrImage(Bitmap qrCodeImage) {
//        // Store the image in the object
//        this.qrCodeImage = qrCodeImage;
//
//        // Convert the bitmap to PNG for upload
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        qrCodeImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//
//        // Upload the image to firebase
//        UploadTask uploadTask = storageRef.child(imageUID).putBytes(data);
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            Log.d("QR Code", "Image upload successful");
//        }).addOnFailureListener(exception -> {
//            Log.d("QR Code", "Image upload failed");
//        });
//
//    }

    /**
     * Gets the string of the QR code
     *
     * @return The text stored in the QR code
     */
    public String getQrString() {
        return this.qrString;
    }


    // ChatGPT, How would I use ZXing to generate a QR code?

    /**
     * Generates a QR code from the given text and stores in the database
     *
     * @param text The text stored in the QR code
     * @throws WriterException If the QR code fails to write
     */
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
        // Store QRCode
        database.storeImageInFirestore(text, null, ImageType.eventQRCodes, bmp, null);
    }

}
