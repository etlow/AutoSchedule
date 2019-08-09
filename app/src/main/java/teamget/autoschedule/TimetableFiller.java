package teamget.autoschedule;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Timetable;

class TimetableFiller {
    static final int HORIZONTAL = 0;
    static final int VERTICAL = 1;

    private int orientation;
    private GridLayout gridLayout;

    TimetableFiller(GridLayout g, int o) {
        gridLayout = g;
        orientation = o;
    }

    TimetableFiller(int o) { orientation = o; }

    TimetableFiller setGridLayout(GridLayout g) {
        gridLayout = g;
        return this;
    }

    void fill(Timetable timetable) {
        if (gridLayout == null) return;
        switch (orientation) {
            case HORIZONTAL: fillHorizontal(timetable); break;
            case VERTICAL: fillVertical(timetable); break;
        }
    }

    private void fillHorizontal(Timetable timetable) {
        Context context = gridLayout.getContext();
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
            gridLayout.addView(makeTextViewH(context, Integer.toString(i / 60), 0,
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
        String[] days = DateFormatSymbols.getInstance().getShortWeekdays();
        for (int i = 0; i <= lastDay; i++) {
            gridLayout.addView(makeTextViewH(context, days[(i + 1) % 7 + 1],
                    Integer.MAX_VALUE, Gravity.CENTER,
                    GridLayout.spec(i + 1, 1), GridLayout.spec(0, 1),
                    Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL));
        }
        // Events
        for (Event event : timetable.events) {
            Lesson lesson = event.options.get(0).list.get(0);
            TextView textView = makeTextViewH(context,
                    lesson.moduleCode + "\n" + lesson.type.substring(0, 3)
                            + "\n" + lesson.location.code, 0,
                    Gravity.CENTER, GridLayout.spec(event.day + 1, 1),
                    getSpec(times, event.startMinutes, event.endMinutes), Gravity.FILL);
            textView.setTextSize(8);
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            gridLayout.addView(textView);
        }
    }

    private void fillVertical(Timetable timetable) {
        Context context = gridLayout.getContext();
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
        gridLayout.setColumnCount(lastDay + 2);
        gridLayout.setRowCount(times.size() * 2);
        // Spacers
        for (int i = 1; i < times.size(); i++) {
            TextView textView = new TextView(context);
            textView.setHeight(0);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(0, 1);
            params.rowSpec = GridLayout.spec(i * 2, 1,
                    times.get(i) - times.get(i - 1));
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        // Hours
        int firstHourInMinutes = earliest;
        if (earliest % 60 != 0) firstHourInMinutes += 60 - earliest % 60;
        for (int i = firstHourInMinutes; i < latest; i += 60) {
            int end = latest - i > 60 ? i + 60 : latest;
            gridLayout.addView(makeTextViewV(context, Integer.toString(i / 60), 0,
                    Integer.MAX_VALUE, Gravity.NO_GRAVITY, GridLayout.spec(0, 1),
                    getSpec(times, i, end), Gravity.FILL_VERTICAL));
            // Horizontal lines
            View view = new View(context);
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = 1;
            params.columnSpec = GridLayout.spec(0, lastDay + 2);
            params.rowSpec = GridLayout
                    .spec(Collections.binarySearch(times, i) * 2 + 1, 1);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            view.setLayoutParams(params);
            gridLayout.addView(view);
        }
        // Days of the week
        String[] days = DateFormatSymbols.getInstance().getShortWeekdays();
        for (int i = 0; i <= lastDay; i++) {
            gridLayout.addView(makeTextViewV(context, days[(i + 1) % 7 + 1],
                    Integer.MAX_VALUE, 0, Gravity.CENTER,
                    GridLayout.spec(i + 1, 1, 1), GridLayout.spec(0, 1),
                    Gravity.FILL));
        }
        // Events
        for (Event event : timetable.events) {
            Lesson lesson = event.options.get(0).list.get(0);
            TextView textView = makeTextViewV(context,
                    lesson.moduleCode + "\n" + lesson.type.substring(0, 3)
                            + "\n" + lesson.location.code, 0, 0,
                    Gravity.CENTER, GridLayout.spec(event.day + 1, 1, 1),
                    getSpec(times, event.startMinutes, event.endMinutes), Gravity.FILL);
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            gridLayout.addView(textView);
        }
    }

    private static GridLayout.Spec getSpec(List<Integer> list, int start, int end) {
        int indexStart = Collections.binarySearch(list, start);
        int indexEnd = Collections.binarySearch(list, end);
        return GridLayout.spec(indexStart * 2 + 2, (indexEnd - indexStart) * 2 - 1,
                end - start);
    }

    private static TextView makeTextViewH(Context context, String text, int maxWidth, int gravity,
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

    private static TextView makeTextViewV(Context context, String text,
                                          int maxHeight, int maxWidth, int gravity,
                                          GridLayout.Spec columnSpec, GridLayout.Spec rowSpec,
                                          int paramGravity) {
        final int margin = 2;
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setMaxHeight(maxHeight);
        textView.setMaxWidth(maxWidth);
        textView.setGravity(gravity);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = columnSpec;
        params.rowSpec = rowSpec;
        params.setGravity(paramGravity);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);
        return textView;
    }

}
