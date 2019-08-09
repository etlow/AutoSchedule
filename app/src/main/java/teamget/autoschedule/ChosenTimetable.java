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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.gridlayout.widget.GridLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teamget.autoschedule.schedule.Timetable;

public class ChosenTimetable extends AppCompatActivity {
    private static final String TAG = "ChosenTimetable";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_timetable);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storeData();

        // To make app subsequently launch into this activity by default
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(getString(R.string.is_setup), Boolean.TRUE);
        edit.apply();

        SharedPreferences pref = getSharedPreferences("ChosenTimetable", MODE_PRIVATE);
        String timetableStr = pref.getString("timetable", null);
        Gson gson = new Gson();
        Timetable timetable = gson.fromJson(timetableStr, Timetable.class);
        GridLayout gridLayout = findViewById(R.id.chosenTimetableGrid);
        new TimetableFiller(gridLayout, TimetableFiller.VERTICAL).fill(timetable);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, ChosenTimetableWidget.class));
        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateIntent);
    }

    private void updateLogInOut(Menu menu, FirebaseAuth firebaseAuth) {
        String text;
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            // already signed in
            text = "Log out";
        } else {
            // not signed in or anonymous
            text = "Log in";
        }
        menu.findItem(R.id.action_log_in_out).setTitle(text);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chosen_timetable_action_bar, menu);
        FirebaseAuth.getInstance().addAuthStateListener(auth -> updateLogInOut(menu, auth));
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        updateLogInOut(menu, FirebaseAuth.getInstance());
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_events:
                // Add ad-hoc events
                return true;

            case R.id.action_edit_modules:
                Intent intent = new Intent(this, ModuleInput.class);
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
                if (item.getTitle().length() == 7) {
                    signOut();
                } else {
                    startSignIn();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void startSignIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            // not signed in
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    // new AuthUI.IdpConfig.FacebookBuilder().build(),
                    // new AuthUI.IdpConfig.TwitterBuilder().build(),
                    new AuthUI.IdpConfig.AnonymousBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .enableAnonymousUsersAutoUpgrade()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.autoschedule_logo)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // user is now signed out
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                            .edit().clear().apply();
                    getSharedPreferences("ModulePreferences", MODE_PRIVATE)
                            .edit().clear().apply();
                    getSharedPreferences("PriorityPreferences", MODE_PRIVATE)
                            .edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
    }

    private void storeData() {
        SharedPreferences modulePrefs = getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        Map<String, ?> map = modulePrefs.getAll();
        Map<String, Object> newMap = new HashMap<>(map);
        Set modules = (Set) newMap.get("modules");
        if (modules != null) newMap.put("modules", new ArrayList<Object>(modules));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
        if (uid == null) return;

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference coll =  db.collection("users").document(uid)
                .collection("data");
        coll.document("ModulePreferences")
                .set(newMap)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

        String timetable = getSharedPreferences("ChosenTimetable", MODE_PRIVATE)
                .getString("timetable", null);
        assert timetable != null;
        Map<String, String> chosen = new HashMap<>();
        chosen.put("timetable", timetable);
        coll.document("ChosenTimetable")
                .set(chosen)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }
}
