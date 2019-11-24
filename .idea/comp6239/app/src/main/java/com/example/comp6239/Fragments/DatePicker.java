package com.example.comp6239.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.widget.TextView;

import com.example.comp6239.R;

import java.util.Calendar;

public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private String TAG="DatePicker Fragment:";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }
    public void populateSetDate(int year, int month, int day) {
        TextView date_textView=getActivity().findViewById(R.id.textView_date);
        date_textView.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
        Log.d(TAG,"Date chosen");
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        populateSetDate(year, month, dayOfMonth);

    }
}