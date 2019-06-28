package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teamget.autoschedule.mods.Module;
import teamget.autoschedule.mods.SampleModules;
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
            modules.add(SampleModules.getModuleByCode(s));
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
        findViewById(R.id.buttonBasic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Top5Timetables.this, NUSTimetableActivity.class);
                startActivity(intent);
            }
        });

        // Include option to open another 5
    }

    // OnClick: Choose timetable -> TimetableViewer activity
}
