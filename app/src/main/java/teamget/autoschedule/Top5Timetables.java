package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.mods.Module;
import teamget.autoschedule.mods.SampleModules;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Priority;
import teamget.autoschedule.schedule.Timetable;
import teamget.autoschedule.schedule.TimetableGeneration;
import teamget.autoschedule.schedule.TimetableScoring;
import teamget.autoschedule.schedule.TypeAdapter;

public class Top5Timetables extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top5_timetables);

        // Receive SP for modules and priorities
        SharedPreferences modulePrefs = getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        Set<String> moduleSet = modulePrefs.getStringSet("modules", null);
        List<String> moduleCodes = new ArrayList<String>(moduleSet);
        List<Module> modules = new ArrayList<>();
        for (String s : moduleCodes) {
            modules.add(SampleModules.getModuleByCode(s, getApplicationContext()));
        }

        SharedPreferences priorityPrefs = getSharedPreferences("PriorityPreferences", MODE_PRIVATE);
        Set<String> prioritySet = priorityPrefs.getStringSet("priorities", null);
        List<String> moduleJson = new ArrayList<String>(prioritySet);
        List<Priority> priorities = new ArrayList<>();

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Priority.class, new TypeAdapter())
                .create();
        for (String s : moduleJson) {
            Priority p = gson.fromJson(s, Priority.class);
            priorities.add(p);
            // For testing
            Log.d("priority check", "priority rank: " + p.rank);
        }

        // For testing
        for (String s : moduleCodes) {
            Log.d("module check", "module: " + s);
        }

        // Generate list of timetables
        TimetableGeneration tg = new TimetableGeneration();
        tg.setModules(modules);
        List<Timetable> timetables = tg.getListOfTimetables();

        // Calculate score, tag score, arrange in decreasing order
        new TimetableScoring(priorities).arrangeTimetablesByScore(timetables);

        // Present top 5 timetables

        List<LinearLayout> layouts = new ArrayList<>();
        LinearLayout linearLayout0 = findViewById(R.id.timetable0);
        layouts.add(linearLayout0);
        LinearLayout linearLayout1 = findViewById(R.id.timetable1);
        layouts.add(linearLayout1);
        LinearLayout linearLayout2 = findViewById(R.id.timetable2);
        layouts.add(linearLayout2);
        LinearLayout linearLayout3 = findViewById(R.id.timetable3);
        layouts.add(linearLayout3);
        LinearLayout linearLayout4 = findViewById(R.id.timetable4);
        layouts.add(linearLayout4);

        for (int grid = 0; grid < 5; grid++) {
            GridLayout gridLayout = new GridLayout(getApplicationContext());
            gridLayout.setColumnCount(12);
            gridLayout.setRowCount(6);
            gridLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            for (int i = 0; i < 11; i++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setText(Integer.toString(i + 8));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(0, 1);
                params.columnSpec = GridLayout.spec(i + 1, 1, 1);
                params.setGravity(Gravity.FILL_HORIZONTAL);
                textView.setLayoutParams(params);
                gridLayout.addView(textView);
            }
            DayOfWeek[] days = DayOfWeek.values();
            for (int i = 0; i < 5; i++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setText(days[i].name().substring(0, 3));
                textView.setGravity(Gravity.CENTER);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i + 1, 1);
                params.columnSpec = GridLayout.spec(0, 1);
                params.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                textView.setLayoutParams(params);
                gridLayout.addView(textView);
            }
            for (Event event : timetables.get(grid).events) {
                TextView textView = new TextView(getApplicationContext());
                Lesson lesson = event.options.get(0).list.get(0);
                textView.setText(lesson.moduleCode + "\n" + lesson.type.substring(0, 3) + "\n" + lesson.location.code);
                textView.setTextSize(8);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(event.day + 1, 1);
                params.columnSpec = GridLayout.spec(event.startHour - 7, event.endHour - event.startHour);
                params.setGravity(Gravity.FILL_HORIZONTAL);
                textView.setLayoutParams(params);
                gridLayout.addView(textView);
            }
            layouts.get(grid).addView(gridLayout);
        }

        getSharedPreferences("ChosenTimetable", MODE_PRIVATE)
                .edit()
                .putString("timetable", gson.toJson(timetables.get(0)))
                .apply();
        Intent intent = new Intent(this, ChosenTimetable.class);
        startActivity(intent);

        // Include option to open another 5
    }

    // OnClick: Choose timetable -> TimetableViewer activity
}
