package com.oopsipushedtomain;

import com.oopsipushedtomain.Database.ImageType;
import com.oopsipushedtomain.QRCode.QRCode;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class QRCodeUnitTest {

    QRCode qrCode;

    // TODO: change this
    String eventID = "EVNT-AJQD6LJ4A4A5MG2EOWUQ";
    ImageType imageType = ImageType.eventQRCodes;

    @Before
    public void setUp() {
        qrCode = null;
    }

    @Test
    public void testCreateNewQRCodeObject() {

        // Act
        qrCode = QRCode.createNewQRCodeObject(eventID, imageType);

        // Assert
        assertNotNull(qrCode.getQRCodeImage());
    }

    @Test
    public void testUpdateQRCodeFromDatabase() throws ExecutionException, InterruptedException, TimeoutException {
        // Use CompletableFuture to asynchronously load the QRCode object and wait for it to complete with a timeout
        qrCode = QRCode.loadQRCodeObject(eventID, imageType).get();

        // Now that we have waited for the QRCode object to load, we can check the image
        assertNotNull("QR Code image should not be null", qrCode.getQRCodeImage());
    }
}
