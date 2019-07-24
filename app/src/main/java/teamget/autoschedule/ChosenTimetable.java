package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.time.DayOfWeek;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Timetable;

public class ChosenTimetable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_timetable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = getSharedPreferences("ChosenTimetable", MODE_PRIVATE);
        String timetableStr = pref.getString("timetable", null);
        Gson gson = new Gson();
        Timetable timetable = gson.fromJson(timetableStr, Timetable.class);
        GridLayout gridLayout = new GridLayout(getApplicationContext());
        gridLayout.setColumnCount(6);
        gridLayout.setRowCount(12);
        gridLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        for (int i = 0; i < 11; i++) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(Integer.toString(i + 8));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(i + 1, 1, 1);
            params.columnSpec = GridLayout.spec(0, 1);
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; i < 5; i++) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(days[i].name().substring(0, 3));
            textView.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(0, 1);
            params.columnSpec = GridLayout.spec(i + 1, 1, 1);
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        for (Event event : timetable.events) {
            TextView textView = new TextView(getApplicationContext());
            Lesson lesson = event.options.get(0).list.get(0);
            textView.setText(lesson.moduleCode + "\n" + lesson.type.substring(0, 3) + "\n" + lesson.location.code);
            textView.setTextSize(8);
            textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(event.startHour - 7, event.endHour - event.startHour);
            params.columnSpec = GridLayout.spec(event.day + 1, 1);
            params.setGravity(Gravity.FILL_VERTICAL | Gravity.FILL_HORIZONTAL);
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        LinearLayout linearLayout = findViewById(R.id.chosenTimetableLinear);
        linearLayout.addView(gridLayout);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chosen_timetable_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_events:
                // Add ad-hoc events
                return true;

            case R.id.action_edit_modules:
                Intent intent = new Intent(this, ModuleInput.class);
                startActivity(intent);
                return true;

            case R.id.action_edit_priorities:
                intent = new Intent(this, PriorityInput.class);
                startActivity(intent);
                return true;

            case R.id.action_choose_other_timetable:
                intent = new Intent(this, Top5Timetables.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
