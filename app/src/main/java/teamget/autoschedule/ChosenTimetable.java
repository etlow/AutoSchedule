package teamget.autoschedule;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.Locale;

import teamget.autoschedule.schedule.Timetable;

public class ChosenTimetable extends AppCompatActivity {
    private TimetablePreferences timetablePreferences = TimetablePreferences.getInstance();
    private AuthFragment authFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_timetable);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        authFragment = AuthFragment.getInstance(getSupportFragmentManager());
        timetablePreferences.uploadData(this);

        // To make app subsequently launch into this activity by default
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(getString(R.string.is_setup), Boolean.TRUE);
        edit.apply();

        SharedPreferences pref = timetablePreferences.getPreferences(this);
        String timetableStr = pref.getString("timetable", null);
        if (timetableStr != null) {
            findViewById(R.id.emptyTimetableText).setVisibility(View.GONE);
            Gson gson = new Gson();
            Timetable timetable = gson.fromJson(timetableStr, Timetable.class);
            GridLayout gridLayout = findViewById(R.id.chosenTimetableGrid);
            new TimetableFiller(gridLayout, TimetableFiller.VERTICAL).fill(timetable);
            Log.v("ChosenTimetable", timetablePreferences.getCurr(this));
        }

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, ChosenTimetableWidget.class));
        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateIntent);
    }

    private void updateMenuText(Menu menu, FirebaseAuth firebaseAuth) {
        int text;
        int deleteText;
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            // already signed in
            text = R.string.chosen_timetable_menu_log_out;
            deleteText = R.string.chosen_timetable_menu_delete_account;
        } else {
            // not signed in or anonymous
            text = R.string.chosen_timetable_menu_log_in;
            deleteText = R.string.chosen_timetable_menu_delete_data;
        }
        menu.findItem(R.id.action_log_in_out).setTitle(text);
        menu.findItem(R.id.action_delete_account).setTitle(deleteText);

        int moduleText;
        boolean visible;
        SharedPreferences pref = timetablePreferences.getPreferences(this);
        if (pref.getStringSet("modules", null) == null) {
            moduleText = R.string.chosen_timetable_menu_modules_add;
            visible = false;
        } else {
            moduleText = R.string.chosen_timetable_menu_modules_edit;
            visible = true;
        }
        menu.findItem(R.id.action_edit_modules).setTitle(moduleText);
        menu.findItem(R.id.action_edit_priorities).setVisible(visible);
        menu.findItem(R.id.action_choose_other_timetable).setVisible(visible);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chosen_timetable_action_bar, menu);
        FirebaseAuth.getInstance().addAuthStateListener(auth -> updateMenuText(menu, auth));
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        updateMenuText(menu, FirebaseAuth.getInstance());
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_modules:
                Intent intent = new Intent(this, SemesterSelection.class);
                startActivity(intent);
                return true;

            case R.id.action_edit_priorities:
                intent = new Intent(this, PriorityInput.class);
                startActivity(intent);
                return true;

            case R.id.action_choose_other_timetable:
                intent = new Intent(this, Top5Timetables.class);
                startActivity(intent);
                return true;

            case R.id.action_log_in_out:
                if (item.getTitle().equals(getString(R.string.chosen_timetable_menu_log_out))) {
                    authFragment.signOut(this).addOnCompleteListener(task -> {
                        // user is now signed out
                        Intent in = new Intent(this, MainActivity.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(in);
                        finish();
                    });
                } else {
                    authFragment.startSignIn(() -> {
                        Intent in = new Intent(this, ChosenTimetable.class);
                        startActivity(in);
                    }, id -> Snackbar.make(findViewById(R.id.textView3), id, Snackbar.LENGTH_LONG));
                }
                return true;

            case R.id.action_delete_account:
                authFragment.deleteAccount(this).addOnSuccessListener(aVoid -> {
                    Intent in = new Intent(this, MainActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(in);
                }).addOnFailureListener(exception -> {
                    String message = String.format(Locale.getDefault(),
                            getString(R.string.chosen_timetable_delete_account_fail),
                            exception.getMessage());
                    Snackbar.make(findViewById(R.id.chosenTimetableGrid), message,
                            Snackbar.LENGTH_LONG);
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
