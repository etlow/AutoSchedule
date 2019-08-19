package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    private static final int NUM_CONSIDER = 10;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Timetable> timetables;

//    SharedPreferences timetablePref;
//    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top5_timetables);
//        timetablePref = getApplicationContext().getSharedPreferences("TimetablePreferences", MODE_PRIVATE);
//        spEditor = timetablePref.edit();

        // Receive SP for modules and priorities
        SharedPreferences preferences = TimetablePreferences.getInstance()
                .getPreferences(this);
        int semester = preferences.getInt("semester", 0);
        Set<String> moduleSet = preferences.getStringSet("modules", Collections.emptySet());
        List<String> moduleCodes = new ArrayList<>(moduleSet);
        List<Module> modules = new ArrayList<>();
        for (String s : moduleCodes) {
            modules.add(SampleModules.getModuleByCode(semester, s, getApplicationContext()));
        }

        Set<String> prioritySet = preferences.getStringSet("priorities", Collections.emptySet());
        List<String> moduleJson = new ArrayList<>(prioritySet);
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
            if (firstSpace > secondSpace
                    * itemsForScheduling.get(itemsForScheduling.size() - 1).size()) {
                List<List<Event>> item = itemsForScheduling.remove(itemsForScheduling.size() - 1);
                secondList.add(item);
                secondSpace *= item.size();
            } else {
                List<List<Event>> item = itemsForScheduling.remove(0);
                firstList.add(item);
                firstSpace *= item.size();
            }
            Log.v("space", "First: " + firstSpace + ", Second: " + secondSpace);
        }

        // First stage list of timetables
        TimetableGeneration tg1 = new TimetableGeneration();
        tg1.setEvents(Collections.emptyList(), firstList);
        List<Timetable> firstStage = tg1.getListOfTimetables();

        // Calculate score, tag score, arrange in decreasing order
        new TimetableScoring(priorities).arrangeTimetablesByScore(firstStage);

        timetables = new ArrayList<>();
        for (int i = 0; i < firstStage.size() && i < NUM_CONSIDER; i++) {
            Log.v("Top5Timetables", "Second (" + i + ")");
            TimetableGeneration tg2 = new TimetableGeneration();
            tg2.setEvents(firstStage.get(i).events, secondList);
            timetables.addAll(tg2.getListOfTimetables());
        }

        // Calculate score, tag score, arrange in decreasing order
        new TimetableScoring(priorities).arrangeTimetablesByScore(timetables);

        // Present top timetables
        recyclerView = findViewById(R.id.generatedTimetables);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TimetableRecyclerAdapter(timetables, this::onButtonClick);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void onButtonClick(int position) {
        final Gson gson = new Gson();
        TimetablePreferences.getInstance().getPreferences(this).edit()
                .putString("timetable", gson.toJson(timetables.get(position)))
                .apply();
        Intent intent = new Intent(this, ChosenTimetable.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
