package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.mods.Module;
import teamget.autoschedule.mods.Option;
import teamget.autoschedule.mods.SampleModules;
import teamget.autoschedule.schedule.Event;
import teamget.autoschedule.schedule.Priority;
import teamget.autoschedule.schedule.Timetable;
import teamget.autoschedule.schedule.TimetableGeneration;
import teamget.autoschedule.schedule.TimetableScoring;
import teamget.autoschedule.schedule.TypeAdapter;

public class Top5Timetables extends AppCompatActivity {
    Button select0, select1, select2, select3, select4;
    int selectedTimetable;
    List<Timetable> timetables;

    private static final int NUM_CONSIDER = 10;

//    SharedPreferences timetablePref;
//    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top5_timetables);
//        timetablePref = getApplicationContext().getSharedPreferences("TimetablePreferences", MODE_PRIVATE);
//        spEditor = timetablePref.edit();

        // Receive SP for modules and priorities
        SharedPreferences modulePrefs = getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        int semester = modulePrefs.getInt("semester", 0);
        Set<String> moduleSet = modulePrefs.getStringSet("modules", null);
        List<String> moduleCodes = new ArrayList<String>(moduleSet);
        List<Module> modules = new ArrayList<>();
        for (String s : moduleCodes) {
            modules.add(SampleModules.getModuleByCode(semester, s, getApplicationContext()));
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

        List<List<List<Event>>> itemsForScheduling = new ArrayList<>();
        for (Module module : modules) {
            for (List<Option> options : module.list) {
                itemsForScheduling.add(TimetableGeneration.toEventList(options));
            }
        }

        Collections.sort(itemsForScheduling, (opts, other) -> opts.size() - other.size());

        List<List<List<Event>>> firstList = new ArrayList<>();
        List<List<List<Event>>> secondList = new ArrayList<>();
        int firstSpace = 1;
        int secondSpace = 1;
        while (!itemsForScheduling.isEmpty()) {
            if (firstSpace > secondSpace) {
                List<List<Event>> item = itemsForScheduling.remove(itemsForScheduling.size() - 1);
                secondList.add(item);
                secondSpace *= item.size();
            } else {
                List<List<Event>> item = itemsForScheduling.remove(0);
                firstList.add(item);
                firstSpace *= item.size();
            }
        }
        Log.v("space", "First: " + firstSpace + ", Second: " + secondSpace);

        // First stage list of timetables
        TimetableGeneration tg1 = new TimetableGeneration();
        tg1.setEvents(Collections.emptyList(), firstList);
        List<Timetable> firstStage = tg1.getListOfTimetables();

        // Calculate score, tag score, arrange in decreasing order
        new TimetableScoring(priorities).arrangeTimetablesByScore(firstStage);

        timetables = new ArrayList<>();
        for (int i = 0; i < firstStage.size() && i < NUM_CONSIDER; i++) {
            TimetableGeneration tg2 = new TimetableGeneration();
            tg2.setEvents(firstStage.get(i).events, secondList);
            timetables.addAll(tg2.getListOfTimetables());
        }

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

        select0 = (Button) findViewById(R.id.button0);
        select1 = (Button) findViewById(R.id.button1);
        select2 = (Button) findViewById(R.id.button2);
        select3 = (Button) findViewById(R.id.button3);
        select4 = (Button) findViewById(R.id.button4);

        // Include option to open another 5
    }

    public void onButtonClick(View v) {
        switch(v.getId()) {
            case R.id.button0:
                selectedTimetable = 0;
                break;
            case R.id.button1:
                selectedTimetable = 1;
                break;
            case R.id.button2:
                selectedTimetable = 2;
                break;
            case R.id.button3:
                selectedTimetable = 3;
                break;
            case R.id.button4:
                selectedTimetable = 4;
        }

        final Gson gson = new Gson();
        getSharedPreferences("ChosenTimetable", MODE_PRIVATE)
                .edit()
                .putString("timetable", gson.toJson(timetables.get(selectedTimetable)))
                .apply();
        Intent intent = new Intent(this, ChosenTimetable.class);
        startActivity(intent);
    }
}
