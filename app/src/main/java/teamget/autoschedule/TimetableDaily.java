package teamget.autoschedule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import teamget.autoschedule.schedule.Event;

public class TimetableDaily extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_daily);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Event> timetable = new ArrayList<>();
        // Receive timetable from Top5Timetables screen

        if (timetable != null && !timetable.isEmpty()) {
            // TBC
        }
    }
}
