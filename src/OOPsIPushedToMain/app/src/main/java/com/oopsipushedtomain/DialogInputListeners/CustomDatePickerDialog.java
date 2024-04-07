package com.oopsipushedtomain.DialogInputListeners;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;


public class CustomDatePickerDialog {
    private Context context;
    private DialogInputListener listener;

    public CustomDatePickerDialog(Context context, DialogInputListener listener) {
        this.context = context;
        this.listener = listener;
    }


    // ChatGPT: How do I show a date picker in android using Java
    public void show(String title, Date defaultValue) {
        // Create a calendar instance
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(defaultValue);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    /// Create a Calendar instance and set the year, month, and day
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, year1);
                    calendar1.set(Calendar.MONTH, monthOfYear);
                    calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Get the Date object
                    Date date = calendar1.getTime();

                    // Output to the listener
                    listener.onInputReceived(date);
                }, year, month, day);


        // Show the date picker
        datePickerDialog.show();

    }
}
