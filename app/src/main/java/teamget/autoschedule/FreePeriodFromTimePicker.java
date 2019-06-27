package teamget.autoschedule;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import java.util.Calendar;
import android.widget.TimePicker;

public class FreePeriodFromTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    ListFragment lf = null;
    String day = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;

        TimePickerDialog tpd = new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, hour, minute, DateFormat.is24HourFormat(getActivity()));

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText("I want to be free on " + day.substring(0,3) + " from...");
        tvTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tvTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        tvTitle.setPadding(5, 3, 5, 3);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tpd.setCustomTitle(tvTitle);

        return tpd;
    }

    public void linkListFragment(ListFragment lf) {
        this.lf = lf;
    }
    public void linkDay(String day) { this.day = day; }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        DialogFragment toTime = new FreePeriodToTimePicker();
        ((FreePeriodToTimePicker) toTime).linkDay(day);
        ((FreePeriodToTimePicker) toTime).linkFromTime(hourOfDay, minute);
        ((FreePeriodToTimePicker) toTime).linkListFragment(lf);
        toTime.show(getActivity().getSupportFragmentManager(), "TimePicker");
    }
}
