package teamget.autoschedule;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import java.util.Calendar;
import android.widget.TimePicker;

import teamget.autoschedule.schedule.AvoidLessonsBeforePriority;

public class BeforeTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    ListFragment lf = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;

        TimePickerDialog tpd = new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, hour, minute, DateFormat.is24HourFormat(getActivity()));

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText("Avoid lessons before...");
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

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String text = String.format("Avoid lessons before %d:%02d.", hourOfDay, minute);
        lf.addItem(text, new AvoidLessonsBeforePriority(0, hourOfDay));
    }
}
