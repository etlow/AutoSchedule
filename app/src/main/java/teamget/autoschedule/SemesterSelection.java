package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import teamget.autoschedule.mods.SampleModules;

public class SemesterSelection extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner year, semester;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_selection);

        pref = getApplicationContext().getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        editor = pref.edit();

        year = findViewById(R.id.yearspinner);
        String[] years = new String[]{"2018-2019", "2019-2020"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(yearAdapter);
        year.setOnItemSelectedListener(this);

        semester = findViewById(R.id.semesterspinner);
        String[] semesters = new String[]{"Semester 1", "Semester 2", "Special Term I", "Special Term II"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semester.setAdapter(semesterAdapter);
        semester.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == year) {
            String acadYear = parent.getItemAtPosition(pos).toString();
            SampleModules.downloadModules(acadYear, getApplicationContext());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // nothing
    }

    public void onButtonClick(View v) {
        String acadYear = String.valueOf(year.getSelectedItem());
        String acadSem = String.valueOf(semester.getSelectedItem());
        int acadSemID = 0;

        switch (acadSem) {
            case "Semester 1":
                acadSemID = 1;
                break;
            case "Semester 2":
                acadSemID = 2;
                break;
            case "Special Term I":
                acadSemID = 3;
                break;
            case "Special Term II":
                acadSemID = 4;
                break;
        }

        editor.putString("year", acadYear);
        editor.putInt("semester", acadSemID);
        editor.apply();

        Intent intent = new Intent(this, ModuleInput.class);
        startActivity(intent);
    }
}
