package teamget.autoschedule;

import teamget.autoschedule.mods.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nex3z.flowlayout.FlowLayout;

public class ModuleInput extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ListView listView;
    SearchView searchView;
    ArrayAdapter<String> adapter;
    ArrayList<String> moduleCodes;
    SharedPreferences modulePref;
    SharedPreferences.Editor spEditor;
    ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        moduleAdapter = new ModuleAdapter();

        modulePref = getApplicationContext().getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        spEditor = modulePref.edit();

        searchView = (SearchView) findViewById(R.id.module_searchview);
        listView = (ListView) findViewById(R.id.module_listview);
        moduleCodes = new ArrayList<>(SampleModules.getModuleCodes(getApplicationContext()));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, moduleCodes);
        listView.setAdapter(adapter);
        searchView.setOnQueryTextListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String modCode = (String) parent.getItemAtPosition(position);
                List<Integer> semesters =
                        SampleModules.getModuleSemesters(modCode, getApplicationContext());
                Integer currSem = modulePref.getInt("semester", 0);
                if (!semesters.contains(currSem)) {
                    Snackbar.make(findViewById(android.R.id.content),
                            modCode + " is unavailable in the current semester.",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                SampleModules.download(modCode, getApplicationContext());

                Set<String> curr = modulePref.getStringSet("modules", Collections.<String>emptySet());
                HashSet<String> newSet = new HashSet<>(curr);
                newSet.add(modCode);

                spEditor.putStringSet("modules", newSet);
                spEditor.commit();

                // To-do: Stick selected modules to the top of list with "Added" tag
                Snackbar.make(findViewById(android.R.id.content),
                        parent.getItemAtPosition(position).toString() + " added!",
                        Snackbar.LENGTH_LONG).show();

                addModuleToSelected(modCode);
            }
        });
    }

    public void addModuleToSelected(String text) {
        FlowLayout flowLayout = findViewById(R.id.selected_mods);
        TextView textView = buildLabel(text);
        flowLayout.addView(textView);
        moduleAdapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.module_input_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                Intent intent = new Intent(this, PriorityInput.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (moduleCodes.contains(query)){
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

    private TextView buildLabel(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setPadding((int)dpToPx(16), (int)dpToPx(8), (int)dpToPx(16), (int)dpToPx(8));
        textView.setBackgroundResource(R.drawable.label_bg);

        return textView;
    }

    private float dpToPx(float dp){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
