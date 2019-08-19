package teamget.autoschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    // First launch welcome screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v) {
        AuthFragment.getInstance(getSupportFragmentManager()).checkSignInAndContinue(() -> {
            Intent intent = new Intent(this, ChosenTimetable.class);
            startActivity(intent);
        }, id -> Snackbar.make(findViewById(R.id.textView3), id, Snackbar.LENGTH_LONG));
    }
}
