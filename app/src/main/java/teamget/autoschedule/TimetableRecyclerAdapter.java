package teamget.autoschedule;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.Consumer;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Timetable;

class TimetableRecyclerAdapter extends RecyclerView.Adapter<TimetableRecyclerAdapter.MyViewHolder> {
    private List<Timetable> timetables;
    private Consumer<Integer> onButtonClick;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constraintLayout;
        private MyViewHolder(ConstraintLayout l) {
            super(l);
            constraintLayout = l;
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

        MyViewHolder viewHolder = new MyViewHolder(constraintLayout);
        constraintLayout.getViewById(R.id.button)
                .setOnClickListener(view -> onButtonClick.accept(viewHolder.getAdapterPosition()));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        ConstraintLayout constraintLayout = viewHolder.constraintLayout;
        GridLayout gridLayout = constraintLayout.findViewById(R.id.gridLayout);
        Context context = constraintLayout.getContext();
        Timetable timetable = timetables.get(position);

        TreeSet<Integer> set = new TreeSet<>();
        int lastDay = 4;
        for (Event event : timetable.events) {
            if (event.day > lastDay) lastDay = event.day;
            set.add(event.startMinutes);
            set.add(event.endMinutes);
        }

        int earliest = set.first();
        int latest = set.last();
        for (int i = earliest - earliest % 60; i < latest; i += 60) { set.add(i); }
        List<Integer> times = new ArrayList<>(set);

        gridLayout.removeAllViews();
        gridLayout.setRowCount(lastDay + 2);
        gridLayout.setColumnCount(times.size() * 2);
        // Spacers
        for (int i = 1; i < times.size(); i++) {
            TextView textView = new TextView(context);
            textView.setWidth(0);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(0, 1);
            params.columnSpec = GridLayout.spec(i * 2, 1,
                    times.get(i) - times.get(i - 1));
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        // Hours
        int firstHourInMinutes = earliest;
        if (earliest % 60 != 0) firstHourInMinutes += 60 - earliest % 60;
        for (int i = firstHourInMinutes; i < latest; i += 60) {
            int end = latest - i > 60 ? i + 60 : latest;
            gridLayout.addView(makeTextView(context, Integer.toString(i / 60), 0,
                    Gravity.NO_GRAVITY, GridLayout.spec(0, 1),
                    getSpec(times, i, end), Gravity.FILL_HORIZONTAL));
            // Vertical lines
            View view = new View(context);
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 1;
            params.rowSpec = GridLayout.spec(0, lastDay + 2);
            params.columnSpec = GridLayout
                    .spec(Collections.binarySearch(times, i) * 2 + 1, 1);
            params.setGravity(Gravity.FILL_VERTICAL);
            view.setLayoutParams(params);
            gridLayout.addView(view);
        }
        // Days of the week
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; i <= lastDay; i++) {
            gridLayout.addView(makeTextView(context, days[i].name().substring(0, 3),
                    Integer.MAX_VALUE, Gravity.CENTER,
                    GridLayout.spec(i + 1, 1), GridLayout.spec(0, 1),
                    Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL));
        }
        // Events
        for (Event event : timetable.events) {
            Lesson lesson = event.options.get(0).list.get(0);
            TextView textView = makeTextView(context,
                    lesson.moduleCode + "\n" + lesson.type.substring(0, 3)
                            + "\n" + lesson.location.code, 0,
                    Gravity.CENTER, GridLayout.spec(event.day + 1, 1),
                    getSpec(times, event.startMinutes, event.endMinutes), Gravity.FILL);
            textView.setTextSize(8);
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            gridLayout.addView(textView);
        }

        String text = String.format(context.getString(R.string
                        .top5timetables_button_selecttimetable), position + 1);
        ((Button) constraintLayout.getViewById(R.id.button)).setText(text);
    }

    @Override
    public int getItemCount() {
        return timetables.size();
    }

    private static GridLayout.Spec getSpec(List<Integer> list, int start, int end) {
        int indexStart = Collections.binarySearch(list, start);
        int indexEnd = Collections.binarySearch(list, end);
        return GridLayout.spec(indexStart * 2 + 2, (indexEnd - indexStart) * 2 - 1,
                end - start);
    }

    private static TextView makeTextView(Context context, String text, int maxWidth, int gravity,
                                         GridLayout.Spec rowSpec, GridLayout.Spec columnSpec,
                                         int paramGravity) {
        final int margin = 2;
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setMaxWidth(maxWidth);
        textView.setGravity(gravity);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = rowSpec;
        params.columnSpec = columnSpec;
        params.setGravity(paramGravity);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);
        return textView;
    }
}
