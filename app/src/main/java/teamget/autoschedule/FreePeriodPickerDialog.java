package teamget.autoschedule;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class FreePeriodPickerDialog extends DialogFragment {
    ListFragment lf;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("I want to be free on...");
        builder.setItems(weekdays, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getActivity(), "You clicked " + weekdays[which], Toast.LENGTH_SHORT).show();
                DialogFragment fromTime = new FreePeriodFromTimePicker();
                ((FreePeriodFromTimePicker) fromTime).linkDay(weekdays[which]);
                ((FreePeriodFromTimePicker) fromTime).linkListFragment(lf);
                fromTime.show(getActivity().getSupportFragmentManager(), "TimePicker");
            }
        });

        return builder.create();
    }

    public void linkListFragment(ListFragment lf) {
        this.lf = lf;
    }
}