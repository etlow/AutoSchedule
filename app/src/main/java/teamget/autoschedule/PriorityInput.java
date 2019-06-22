package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

import java.util.Map;

public class PriorityInput extends AppCompatActivity {

    FloatingActionMenu fam;
    FloatingActionButton avoidLessonsBefore, avoidLessonsAfter, maxFreeDays, freePeriod,
                         minTravelling, minBreaks, lunchBreak;
    SharedPreferences priorityPref;
    SharedPreferences.Editor spEditor;
    int keyCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fam = (FloatingActionMenu) findViewById(R.id.add_priority_fab);
        avoidLessonsBefore = (FloatingActionButton) findViewById(R.id.avoid_lessons_before_fab);
        avoidLessonsAfter = (FloatingActionButton) findViewById(R.id.avoid_lessons_after_fab);
        maxFreeDays = (FloatingActionButton) findViewById(R.id.max_free_days_fab);
        freePeriod = (FloatingActionButton) findViewById(R.id.free_period_fab);
        minTravelling = (FloatingActionButton) findViewById(R.id.min_travelling_fab);
        minBreaks = (FloatingActionButton) findViewById(R.id.min_breaks_fab);
        lunchBreak = (FloatingActionButton) findViewById(R.id.lunch_break_fab);

        priorityPref = getApplicationContext().getSharedPreferences("PriorityPreferences", MODE_PRIVATE);
        spEditor = priorityPref.edit();

        avoidLessonsBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Avoid lessons before", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        avoidLessonsAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Avoid lessons after", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        maxFreeDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Maximum free days", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        freePeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Free period during", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        minTravelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Minimal travelling", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        minBreaks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Minimal breaks during", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });

        lunchBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Lunch break for", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.priority_input_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                // Add priority
                // spEditor.putString("module" + keyCounter, /*smth*/.toString());
                // spEditor.commit();
                // keyCounter++;

                // just to log
                Map<String, ?> allEntries = priorityPref.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                }

                Intent intent = new Intent(this, Top5Timetables.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
