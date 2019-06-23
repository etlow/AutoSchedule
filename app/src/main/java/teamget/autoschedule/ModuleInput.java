package teamget.autoschedule;

import teamget.autoschedule.mods.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleInput extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ListView listView;
    SearchView searchView;
    ArrayAdapter<Module> adapter;
    ArrayList<Module> moduleList = new ArrayList<Module>();
    SharedPreferences modulePref;
    SharedPreferences.Editor spEditor;
    int keyCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = (SearchView) findViewById(R.id.module_searchview);
        listView = (ListView) findViewById(R.id.module_listview);
        for (Module m : SampleModules.getModules()) { moduleList.add(m); }
        adapter = new ArrayAdapter<Module>(this, android.R.layout.simple_list_item_1, moduleList);
        listView.setAdapter(adapter);
        searchView.setOnQueryTextListener(this);

        modulePref = getApplicationContext().getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        spEditor = modulePref.edit();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Module selectedModule = (Module) parent.getItemAtPosition(position);
                spEditor.putString("module" + keyCounter, selectedModule.toString());
                spEditor.commit();
                keyCounter++;

                // To-do: Stick selected modules to the top of list with "Added" tag
                Snackbar.make(findViewById(android.R.id.content),
                        parent.getItemAtPosition(position).toString() + " added!",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.module_input_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                // For testing
                List<Module> selectedModules = new ArrayList<>();
                Map<String, ?> allEntries = modulePref.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.v("ModuleInput test", entry.toString());
                    Log.v("ModuleInput test", entry.getValue().toString());
                    selectedModules.add(SampleModules.getModuleByCode((String) entry.getValue()));
                }
                teamget.autoschedule.schedule.Timetable.test(selectedModules);

                Intent intent = new Intent(this, PriorityInput.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (moduleList.contains(query)){
            adapter.getFilter().filter(query);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "No match found", Snackbar.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }
}
