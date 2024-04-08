/**
 * This file contains the edit dialog fragment.
 * It is created when a user clicks on a field on the profile page
 */
package com.oopsipushedtomain.User;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.oopsipushedtomain.R;

/**
 * The edit dialog fragment for editing the user's profile
 */
public class EditFieldDialogFragment extends DialogFragment {

    /**
     * The edit text shown for editing the data
     */
    private EditText editTextValue;

    /**
     * The dialog listener for the edit field
     */
    EditFieldDialogListener listener;

    /**
     * An interface for defining the functions for performing an action when a button is pressed in the dialog
     */
    public interface EditFieldDialogListener {
        /**
         * Performs a function when the confirm button is pressed on the dialog
         * @param dialog The dialog clicked
         * @param fieldName The name of the field
         * @param fieldValue The value of the field
         */
        void onDialogPositiveClick(DialogFragment dialog, String fieldName, String fieldValue);

        /**
         * Performs a function when the cancel button is pressed on the dialog
         * @param dialog The dialog clicked
         */
        void onDialogNegativeClick(DialogFragment dialog);
    }



    /**
     * Displays the current value of the field in the dialog before allowing editing.
     * Also sets the click listeners for the buttons.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return A reference to the dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_field, null);

        editTextValue = view.findViewById(R.id.editTextFieldValue);

        // Retrieve and display the field name and value from arguments
        Bundle args = getArguments();
        String fieldName = args.getString("fieldName", "");
        String fieldValue = args.getString("fieldValue", "");
        getActivity().setTitle("Edit " + fieldName);
        editTextValue.setText(fieldValue);

        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(EditFieldDialogFragment.this, fieldName, editTextValue.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditFieldDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the EditFieldDialogListener

    /**
     * Check if the calling class implements the EditFieldDialogListener interface
     * @param context The running context
     */
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the EditFieldDialogListener so we can send events to the host
            listener = (EditFieldDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement EditFieldDialogListener");
        }
    }
}
