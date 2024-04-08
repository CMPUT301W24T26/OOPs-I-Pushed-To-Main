package com.oopsipushedtomain.DialogInputListeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Creates a input text dialog and handles the output
 */
public class InputTextDialog {
    /**
     * The context to create the dialog in
     */
    private Context context;
    /**
     * The listener to output the data to
     */
    private DialogInputListener listener;

    /**
     * Creates the class
     * @param context The context
     * @param listener The listener for output
     */
    public InputTextDialog(Context context, DialogInputListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Shows the dialog
     * @param title The title for the dialog
     * @param defaultValue The default value the dialog starts with
     */
    public void show(String title, String defaultValue) {
        // Create an edit text
        EditText editText = new EditText(context);
        editText.setText(defaultValue);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(editText);

        // Set the positive click listener
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = editText.getText().toString();
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
}
