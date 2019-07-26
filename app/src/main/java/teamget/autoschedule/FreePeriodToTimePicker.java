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

import java.time.DayOfWeek;
import java.util.Calendar;
import android.widget.TimePicker;

import teamget.autoschedule.schedule.FreePeriodPriority;

public class FreePeriodToTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    ListFragment lf = null;
    String day;
    int fromTimeHour;
    int fromTimeMinute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;

        TimePickerDialog tpd = new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, hour, minute, DateFormat.is24HourFormat(getActivity()));

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText(String.format("I want to be free on %s from %d:%02d to...",
                        day.substring(0,3), fromTimeHour, fromTimeMinute));
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
    public void linkFromTime(int fromTimeHour, int fromTimeMinute) {
        this.fromTimeHour = fromTimeHour;
        this.fromTimeMinute = fromTimeMinute;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView text = (TextView) getActivity().findViewById(R.id.text_to_fill);
        int dayID = day.equals("Every day") ? 5 : DayOfWeek.valueOf(day.toUpperCase()).ordinal();
        if (dayID == 5) {
            text.setText(String.format("I want to be free every day from %d:%02d to %d:%02d.",
                    fromTimeHour, fromTimeMinute, hourOfDay, minute));
        } else {
            text.setText(String.format("I want to be free on %s from %d:%02d to %d:%02d.",
                    day.substring(0, 3), fromTimeHour, fromTimeMinute, hourOfDay, minute));
        }
        lf.addItem((String) text.getText().toString(), new FreePeriodPriority(0, dayID, fromTimeHour, hourOfDay));
    }
}
