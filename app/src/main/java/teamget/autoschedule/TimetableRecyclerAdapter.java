package teamget.autoschedule;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.Consumer;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.util.List;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Timetable;

class TimetableRecyclerAdapter extends RecyclerView.Adapter<TimetableRecyclerAdapter.MyViewHolder> {
    private List<Timetable> timetables;
    private Consumer<Integer> onButtonClick;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constraintLayout;
        private MyViewHolder(ConstraintLayout l, Consumer<Integer> onButtonClick) {
            super(l);
            constraintLayout = l;
            constraintLayout.getViewById(R.id.button)
                    .setOnClickListener(view -> onButtonClick.accept(getAdapterPosition()));
        }
    }

    TimetableRecyclerAdapter(List<Timetable> t, Consumer<Integer> c) {
        timetables = t;
        onButtonClick = c;
    }

    @Override
    @NonNull
    public TimetableRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.timetable_choice, parent, false);

        return new MyViewHolder(constraintLayout, onButtonClick);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {

        GridLayout gridLayout = viewHolder.constraintLayout.findViewById(R.id.gridLayout);
        Context context = viewHolder.constraintLayout.getContext();
        Timetable timetable = timetables.get(position);

        int lastDay = 4;
        int earliest = timetable.events.get(0).startHour;
        int latest = timetable.events.get(0).endHour;
        for (Event event : timetable.events) {
            if (event.day > lastDay) lastDay = event.day;
            if (event.startHour < earliest) earliest = event.startHour;
            if (event.endHour > latest) latest = event.endHour;
        }

        gridLayout.removeAllViews();
        gridLayout.setRowCount(lastDay + 2);
        gridLayout.setColumnCount(latest - earliest + 1);
        for (int i = 0; i < latest - earliest; i++) {
            gridLayout.addView(makeTextView(context, Integer.toString(i + earliest),
                    Gravity.NO_GRAVITY, GridLayout.spec(0, 1),
                    GridLayout.spec(i + 1, 1, 1), Gravity.FILL_HORIZONTAL));
        }
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; i <= lastDay; i++) {
            gridLayout.addView(makeTextView(context, days[i].name().substring(0, 3), Gravity.CENTER,
                    GridLayout.spec(i + 1, 1), GridLayout.spec(0, 1),
                    Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL));
        }
        for (Event event : timetable.events) {
            Lesson lesson = event.options.get(0).list.get(0);
            TextView textView = makeTextView(context,
                    lesson.moduleCode + "\n" + lesson.type.substring(0, 3)
                            + "\n" + lesson.location.code,
                    Gravity.CENTER, GridLayout.spec(event.day + 1, 1),
                    GridLayout.spec(event.startHour - earliest + 1,
                            event.endHour - event.startHour), Gravity.FILL_HORIZONTAL);
            textView.setTextSize(8);
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            gridLayout.addView(textView);
        }

        String text = String
                .format(context.getString(R.string.top5timetables_button_selecttimetable),
                        position + 1);
        ((Button) viewHolder.constraintLayout.getViewById(R.id.button)).setText(text);
    }

    @Override
    public int getItemCount() {
        return timetables.size();
    }

    private static TextView makeTextView(Context context, String text, int gravity,
                                         GridLayout.Spec rowSpec, GridLayout.Spec columnSpec,
                                         int paramGravity) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(gravity);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = rowSpec;
        params.columnSpec = columnSpec;
        params.setGravity(paramGravity);
        textView.setLayoutParams(params);
        return textView;
    }
}
