package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;
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
    SharedPreferences priorityPref;
    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        priorityPref = getApplicationContext().getSharedPreferences("PriorityPreferences", MODE_PRIVATE);
        spEditor = priorityPref.edit();

        if (savedInstanceState == null) {
            lf = ListFragment.newInstance();
            showFragment(lf);
        }

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
                TextView text = (TextView) findViewById(R.id.text_to_fill);
                text.setText("I want as many free days as possible.");
                lf.addItem((String) text.getText().toString(), new MaxFreeDaysPriority(0));
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
                TextView text = (TextView) findViewById(R.id.text_to_fill);
                text.setText("I want minimal travelling across the \ncampus.");
                lf.addItem((String) text.getText().toString(), new MinimalTravellingPriority(0));
            }
        });

        minBreaks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView text = (TextView) findViewById(R.id.text_to_fill);
                text.setText("I want minimal breaks between classes.");
                lf.addItem((String) text.getText().toString(), new MinimalBreaksPriority(0));
            }
        });

        lunchBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(view);
            }
        });

        /* FIX: NullPointerException -- lf.addItem() -- listAdapter.notifyDataSetChanged()
        // load previously selected priorities
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isAccessed = prefs.getBoolean(getString(R.string.is_setup), false);

        if (isAccessed) {
            Set<String> prioritySet = priorityPref.getStringSet("priorities", null);
            List<String> moduleJson = new ArrayList<String>(prioritySet);

            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Priority.class, new TypeAdapter())
                    .create();
            for (String s : moduleJson) {
                Priority p = gson.fromJson(s, Priority.class);
                if (p instanceof AvoidLessonsBeforePriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    int time = ((AvoidLessonsBeforePriority) p).time;
                    text.setText(String.format("Avoid lessons before %d:00", time));
                    lf.addItem((String) text.getText().toString(), new AvoidLessonsBeforePriority(p.rank, time));
                } else if (p instanceof AvoidLessonsAfterPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    int time = ((AvoidLessonsAfterPriority) p).time;
                    text.setText(String.format("Avoid lessons after %d:00", time));
                    lf.addItem((String) text.getText().toString(), new AvoidLessonsAfterPriority(p.rank, time));
                } else if (p instanceof MaxFreeDaysPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    text.setText("I want as many free days as possible.");
                    lf.addItem((String) text.getText().toString(), new MaxFreeDaysPriority(p.rank));
                } else if (p instanceof FreePeriodPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    int dayID = ((FreePeriodPriority) p).day;
                    int fromTime = ((FreePeriodPriority) p).fromTime;
                    int toTime = ((FreePeriodPriority) p).toTime;
                    if (dayID == 5) {
                        text.setText(String.format("I want to be free every day from %d:00 to %d:00.",
                                fromTime, toTime));
                    } else {
                        String day = "";
                        switch (dayID) {
                            case 0: day = "Mon";
                            case 1: day = "Tue";
                            case 2: day = "Wed";
                            case 3: day = "Thu";
                            case 4: day = "Fri";
                        }
                        text.setText(String.format("I want to be free on %s from %d:00 to %d:00.",
                                day, fromTime, toTime));
                    }
                    lf.addItem((String) text.getText().toString(), new FreePeriodPriority(p.rank, dayID, fromTime, toTime));
                } else if (p instanceof MinimalTravellingPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    text.setText("I want minimal travelling across the campus.");
                    lf.addItem((String) text.getText().toString(), new MinimalTravellingPriority(p.rank));
                } else if (p instanceof MinimalBreaksPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    text.setText("I want minimal breaks between classes.");
                    lf.addItem((String) text.getText().toString(), new MinimalBreaksPriority(p.rank));
                } else if (p instanceof LunchBreakPriority) {
                    TextView text = (TextView) findViewById(R.id.text_to_fill);
                    int hours = ((LunchBreakPriority) p).hours;
                    text.setText(String.format("I want a lunch break of at least %d hours.", hours));
                    lf.addItem((String) text.getText().toString(), new LunchBreakPriority(p.rank, hours));
                }
            }
        }
        */
    }


    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "fragment").commit();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        //Toast.makeText(this, "selected number " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
    }

    public void showNumberPicker(View view){
        NumberPickerDialog newFragment = new NumberPickerDialog();
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "time_picker");
        newFragment.linkListFragment(lf);
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
                Set<String> priorities = priorityPref.getStringSet("priorities", Collections.<String>emptySet());
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
//                Map<String, ?> allEntries = priorityPref.getAll();
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
