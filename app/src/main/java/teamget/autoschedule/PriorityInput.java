package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import teamget.autoschedule.schedule.MaxFreeDaysPriority;
import teamget.autoschedule.schedule.MinimalBreaksPriority;
import teamget.autoschedule.schedule.MinimalTravellingPriority;
import teamget.autoschedule.schedule.Priority;
import teamget.autoschedule.schedule.TypeAdapter;

public class PriorityInput extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    ListFragment lf;

    FloatingActionMenu fam;
    FloatingActionButton avoidLessonsBefore, avoidLessonsAfter, maxFreeDays, freePeriod,
                         minTravelling, minBreaks, lunchBreak;

    //List<Priority> priorities = new ArrayList<>();
    final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Priority.class, new TypeAdapter())
            .create();
    SharedPreferences preferences;
    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = TimetablePreferences.getInstance().getPreferences(this);
        spEditor = preferences.edit();

        Set<String> prioritySet = preferences.getStringSet("priorities", Collections.emptySet());
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Priority.class, new TypeAdapter())
                .create();

        lf = ListFragment.newInstance();
        showFragment(lf).runOnCommit(() -> {
            for (String s : prioritySet) lf.addItem(gson.fromJson(s, Priority.class));
        });

        fam = (FloatingActionMenu) findViewById(R.id.add_priority_fab);
        avoidLessonsBefore = (FloatingActionButton) findViewById(R.id.avoid_lessons_before_fab);
        avoidLessonsAfter = (FloatingActionButton) findViewById(R.id.avoid_lessons_after_fab);
        maxFreeDays = (FloatingActionButton) findViewById(R.id.max_free_days_fab);
        freePeriod = (FloatingActionButton) findViewById(R.id.free_period_fab);
        minTravelling = (FloatingActionButton) findViewById(R.id.min_travelling_fab);
        minBreaks = (FloatingActionButton) findViewById(R.id.min_breaks_fab);
        lunchBreak = (FloatingActionButton) findViewById(R.id.lunch_break_fab);

        avoidLessonsBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment beforeTime = new BeforeTimePicker();
                beforeTime.show(getSupportFragmentManager(),"TimePicker");
                ((BeforeTimePicker) beforeTime).linkListFragment(lf);
            }
        });

        avoidLessonsAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment afterTime = new AfterTimePicker();
                afterTime.show(getSupportFragmentManager(),"TimePicker");
                ((AfterTimePicker) afterTime).linkListFragment(lf);
            }
        });

        maxFreeDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lf.addItem(new MaxFreeDaysPriority(0));
            }
        });

        freePeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FreePeriodPickerDialog newFragment = new FreePeriodPickerDialog();
                newFragment.show(getSupportFragmentManager(), "free_period_picker");
                newFragment.linkListFragment(lf);
            }
        });

        minTravelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lf.addItem(new MinimalTravellingPriority(0));
            }
        });

        minBreaks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lf.addItem(new MinimalBreaksPriority(0));
            }
        });

        lunchBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPickerDialog newFragment = new NumberPickerDialog();
                newFragment.setValueChangeListener(PriorityInput.this);
                newFragment.show(getSupportFragmentManager(), "time_picker");
                newFragment.linkListFragment(lf);
            }
        });
    }


    private FragmentTransaction showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "fragment").commit();
        return transaction;
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        //Toast.makeText(this, "selected number " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.priority_input_action_bar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                Set<String> priorities = preferences.getStringSet("priorities", Collections.<String>emptySet());
                HashSet<String> newSet = new HashSet<>(priorities);

                for (Pair<Long, String> p : lf.mItemArray) {
                    int rank = lf.listAdapter.getPositionForItem(p);
                    Priority priority = lf.priorities.get(p.first);
                    priority.setRank(rank);
                    String json = gson.toJson(priority, Priority.class);
                    newSet.add(json);
                }

                spEditor.putStringSet("priorities", newSet);
                spEditor.commit();

//                // just to log
//                Map<String, ?> allEntries = preferences.getAll();
//                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//                    Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
//                }

                Intent intent = new Intent(this, Top5Timetables.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
