package teamget.autoschedule;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import teamget.autoschedule.mods.SampleModules;

public class MainActivity extends AppCompatActivity {
    // First launch welcome screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Should go in semester selection activity
        SampleModules.downloadModules("2018-2019", getApplicationContext());
    }

    public void onButtonClick(View v) {
        Intent intent = new Intent(this, ModuleInput.class);
        startActivity(intent);
    }
}
