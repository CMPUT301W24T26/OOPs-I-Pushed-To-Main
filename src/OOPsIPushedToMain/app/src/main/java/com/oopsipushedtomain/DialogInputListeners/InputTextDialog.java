package com.oopsipushedtomain.DialogInputListeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class InputTextDialog {

    private Context context;
    private DialogInputListener listener;

    public InputTextDialog(Context context, DialogInputListener listener) {
        this.context = context;
        this.listener = listener;
    }

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
        dialog.show();
    }
}
