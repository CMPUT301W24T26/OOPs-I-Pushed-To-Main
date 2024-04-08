package com.oopsipushedtomain.DialogInputListeners;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class CustomDateTimePickerDialog {
    private Context context;
    private DialogInputListener listener;

    public CustomDateTimePickerDialog(Context context, DialogInputListener listener) {
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
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Create a Calendar instance and set the year, month, and day
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, year1);
                    calendar1.set(Calendar.MONTH, monthOfYear);
                    calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Now show the time picker
                    showTimePicker(calendar1);

                }, year, month, day);


        // Show the date picker
        datePickerDialog.show();
    }

    // Chat GPT: What if I want to add time to the date picker?
    private void showTimePicker(final Calendar calendar) {
        new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Set the hour and minute to the calendar
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        // Now calendar contains both the date and time
                        Date date = calendar.getTime();

                        // Output to the listener
                        listener.onInputReceived(date);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

}
