package teamget.autoschedule;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.schedule.Event;
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
        GridLayout gridLayout = new GridLayout(getApplicationContext());
        populateGridLayout(gridLayout, timetable);

        LinearLayout linearLayout = findViewById(R.id.chosenTimetableLinear);
        linearLayout.addView(gridLayout);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, ChosenTimetableWidget.class));
        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateIntent);
    }

    private void populateGridLayout(GridLayout gridLayout, Timetable timetable) {
        Context context = getApplicationContext();
        TreeSet<Integer> set = new TreeSet<>();
        int lastDay = 4;
        for (Event event : timetable.events) {
            if (event.day > lastDay) lastDay = event.day;
            set.add(event.startMinutes);
            set.add(event.endMinutes);
        }

        int earliest = set.first();
        int latest = set.last();
        for (int i = earliest - earliest % 60; i < latest; i += 60) { set.add(i); }
        List<Integer> times = new ArrayList<>(set);

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(lastDay + 2);
        gridLayout.setRowCount(times.size() * 2);
        gridLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        // Spacers
        for (int i = 1; i < times.size(); i++) {
            TextView textView = new TextView(context);
            textView.setHeight(0);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(0, 1);
            params.rowSpec = GridLayout.spec(i * 2, 1,
                    times.get(i) - times.get(i - 1));
            textView.setLayoutParams(params);
            gridLayout.addView(textView);
        }
        // Hours
        int firstHourInMinutes = earliest;
        if (earliest % 60 != 0) firstHourInMinutes += 60 - earliest % 60;
        for (int i = firstHourInMinutes; i < latest; i += 60) {
            int end = latest - i > 60 ? i + 60 : latest;
            gridLayout.addView(makeTextView(context, Integer.toString(i / 60), 0,
                    Integer.MAX_VALUE, Gravity.NO_GRAVITY, GridLayout.spec(0, 1),
                    getSpec(times, i, end), Gravity.FILL_VERTICAL));
            // Horizontal lines
            View view = new View(context);
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = 1;
            params.columnSpec = GridLayout.spec(0, lastDay + 2);
            params.rowSpec = GridLayout
                    .spec(Collections.binarySearch(times, i) * 2 + 1, 1);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            view.setLayoutParams(params);
            gridLayout.addView(view);
        }
        // Days of the week
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; i <= lastDay; i++) {
            gridLayout.addView(makeTextView(context, days[i].name().substring(0, 3),
                    Integer.MAX_VALUE, 0, Gravity.CENTER,
                    GridLayout.spec(i + 1, 1, 1), GridLayout.spec(0, 1),
                    Gravity.FILL));
        }
        // Events
        for (Event event : timetable.events) {
            Lesson lesson = event.options.get(0).list.get(0);
            TextView textView = makeTextView(context,
                    lesson.moduleCode + "\n" + lesson.type.substring(0, 3)
                            + "\n" + lesson.location.code, 0, 0,
                    Gravity.CENTER, GridLayout.spec(event.day + 1, 1, 1),
                    getSpec(times, event.startMinutes, event.endMinutes), Gravity.FILL);
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            gridLayout.addView(textView);
        }
    }

    private static GridLayout.Spec getSpec(List<Integer> list, int start, int end) {
        int indexStart = Collections.binarySearch(list, start);
        int indexEnd = Collections.binarySearch(list, end);
        return GridLayout.spec(indexStart * 2 + 2, (indexEnd - indexStart) * 2 - 1,
                end - start);
    }

    private static TextView makeTextView(Context context, String text,
                                         int maxHeight, int maxWidth, int gravity,
                                         GridLayout.Spec columnSpec, GridLayout.Spec rowSpec,
                                         int paramGravity) {
        final int margin = 2;
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setMaxHeight(maxHeight);
        textView.setMaxWidth(maxWidth);
        textView.setGravity(gravity);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = columnSpec;
        params.rowSpec = rowSpec;
        params.setGravity(paramGravity);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);
        return textView;
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
