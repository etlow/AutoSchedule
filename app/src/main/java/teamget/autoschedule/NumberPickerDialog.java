package teamget.autoschedule;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.NumberPicker;
import android.widget.TextView;

import teamget.autoschedule.schedule.LunchBreakPriority;

public class NumberPickerDialog extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;
    private ListFragment lf;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(5);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("I want a lunch break of at least...");
        builder.setMessage("Choose number of hours:");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue());
                TextView text = (TextView) getActivity().findViewById(R.id.text_to_fill);
                text.setText(String.format("I want a lunch break of at least %d hours.", numberPicker.getValue()));
                lf.addItem((String) text.getText().toString(), new LunchBreakPriority(0, numberPicker.getValue()));
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue());
            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public void linkListFragment(ListFragment lf) {
        this.lf = lf;
    }
}
