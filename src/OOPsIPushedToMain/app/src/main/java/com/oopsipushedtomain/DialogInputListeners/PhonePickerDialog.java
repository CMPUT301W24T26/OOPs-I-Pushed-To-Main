package com.oopsipushedtomain.DialogInputListeners;


import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * A dialog for entering a phone number
 * Handles formatting
 */
public class PhonePickerDialog {

    /**
     * The conetxt to create the dialog in
     */
    private Context context;
    /**
     * The listener to output to
     */
    private DialogInputListener listener;

    /**
     * Creates the class
     * @param context The context
     * @param listener The listener to output to
     */
    public PhonePickerDialog(Context context, DialogInputListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Shows the dialog
     * @param title The title of the dialog
     * @param defaultValue The default value of the dialog
     */
    public void show(String title, String defaultValue) {
        // Create an edit text
        EditText editText = new EditText(context);
        editText.setText(defaultValue);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(editText);

        // Set the positive click listener
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = editText.getText().toString();

                // Return to the listener
                listener.onInputReceived(userInput);
            }
        });

        // Set the negative click button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();


        // Open the keyboard
        editText.requestFocus();
    }

    /**
     * Formats the phone number
     * @param phoneNumber The number to format
     * @return The formatted number
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            return null; // or throw an exception
        }

        return "(" + phoneNumber.substring(0, 3) + ") "
                + phoneNumber.substring(3, 6) + "-"
                + phoneNumber.substring(6);
    }


}
