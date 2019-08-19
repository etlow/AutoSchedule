package teamget.autoschedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TimetablePreferences {
    private static final String TAG = "TimetablePreferences";
    private static TimetablePreferences instance;
    private String prefName, prefListName, prefCurrName, prefix, fireData, fireTimetables;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String curr;

    public static TimetablePreferences getInstance() {
        if (instance == null) {
            instance = new TimetablePreferences();
            instance.prefName = TAG;
            instance.prefListName = "timetables";
            instance.prefCurrName = "current";
            instance.prefix = "Timetable ";
            instance.fireData = "data";
            instance.fireTimetables = "timetables";
        }
        return instance;
    }

    String getCurr(Context context) {
        if (curr == null) {
            curr = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getString(prefCurrName, null);
        }
        return curr;
    }

    private void setCurr(Context context, String c) {
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .edit().putString(prefCurrName, c).apply();
        curr = c;
    }

    public SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(prefix + getCurr(context), Context.MODE_PRIVATE);
    }

    private SharedPreferences getPreferences(Context context, String name) {
        return context.getSharedPreferences(prefix + name, Context.MODE_PRIVATE);
    }

    private Set<String> listTimetables(Context context) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return pref.getStringSet(prefListName, Collections.emptySet());
    }

    private void addTimetable(Context context, String name) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Set<String> timetables = pref.getStringSet(prefListName, Collections.emptySet());
        Set<String> newSet = new HashSet<>(timetables);
        newSet.add(name);
        pref.edit().putStringSet(prefListName, newSet).apply();
    }

    private void deleteTimetable(Context context, String name) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Set<String> timetables = pref.getStringSet(prefListName, Collections.emptySet());
        Set<String> newSet = new HashSet<>(timetables);
        newSet.remove(name);
        pref.edit().putStringSet(prefListName, newSet).apply();
        getPreferences(context, name).edit().clear().apply();
    }

    private void copyTimetable(Context context, String from, String to) {
        addTimetable(context, to);
        SharedPreferences.Editor editor = getPreferences(context, to).edit();
        for (Map.Entry<String, ?> entry : getPreferences(context, from).getAll().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Set) {
                Set<String> set = new HashSet<>();
                for (Object s : (Set) value) set.add((String) s);
                editor.putStringSet(entry.getKey(), set);
            }
        }
        editor.apply();
    }

    private void setTimetable(Context context, String name, Map<String, Object> map) {
        addTimetable(context, name);
        setPreferences(context, prefix + name, map);
    }

    private void setPreferences(Context context, String name, Map<String, Object> map) {
        SharedPreferences.Editor editor = context
                .getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof List) {
                List list = (List) value;
                Set<String> set = new HashSet<>();
                for (Object str : list) {
                    set.add((String) str);
                }
                editor.putStringSet(entry.getKey(), set);
            }
        }
        editor.apply();
    }

    void clearData(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
        for (String name : listTimetables(context)) deleteTimetable(context, name);
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("ModuleList", Context.MODE_PRIVATE).edit().clear().apply();
    }

    Task<QuerySnapshot> downloadData(Context context) {
        String uid = auth.getUid();
        assert uid != null;
        DocumentReference userDoc = FirebaseFirestore.getInstance()
                .collection("users").document(uid);
        Task<DocumentSnapshot> dataRef = userDoc
                .collection(fireData).document(prefName).get();
        return dataRef.continueWithTask(task -> {
            task.getResult();
            return userDoc.collection(fireTimetables).get();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Set<String> addNameSet = new HashSet<>();
                DocumentSnapshot document = dataRef.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    Map<String, Object> map = document.getData();
                    assert map != null;

                    Object timetableSet = map.get(prefListName);
                    if (timetableSet instanceof List) {
                        for (Object name : (List) timetableSet) addNameSet.add((String) name);
                        renameTimetables(context, addNameSet);
                    }
                }

                QuerySnapshot snapshot = task.getResult();
                assert snapshot != null;
                for (QueryDocumentSnapshot doc : snapshot) {
                    Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                    String name = doc.getId();
                    setTimetable(context, name, doc.getData());
                    if (!addNameSet.contains(name)) Log.w(TAG, "Extra timetable not in set");
                }
                if (snapshot.size() == 0) {
                    addTimetable(context, context.getString(R.string.default_timetable));
                    setCurr(context, context.getString(R.string.default_timetable));
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void renameTimetables(Context context, Set<String> nameSet) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Set<String> toAdd = pref.getStringSet(prefListName, null);
        if (toAdd == null) return;
        for (String toAddName : toAdd) {
            boolean exists = nameSet.contains(toAddName);
            int i = 0;
            String newName = null;
            while (exists) {
                i++;
                newName = String.format(Locale.getDefault(), "%s (%d)", toAddName, i);
                exists = nameSet.contains(newName);
            }
            if (i > 0) {
                nameSet.add(newName);
                copyTimetable(context, toAddName, newName);
                deleteTimetable(context, toAddName);
                if (toAddName.equals(getCurr(context))) setCurr(context, newName);
            }
        }
        pref.edit().putStringSet(prefListName, nameSet).apply();
    }

    void uploadData(Context context) {
        String uid = auth.getUid();
        if (uid == null) return;
        DocumentReference userDoc = FirebaseFirestore.getInstance()
                .collection("users").document(uid);
        CollectionReference coll = userDoc.collection(fireTimetables);

        coll.get().addOnSuccessListener(snapshots -> {
            Set<String> missing = new HashSet<>();
            assert snapshots != null;
            for (QueryDocumentSnapshot snapshot : snapshots) missing.add(snapshot.getId());

            for (String name : listTimetables(context)) {
                missing.remove(name);
                Map<String, Object> map = convertForUpload(getPreferences(context, name).getAll());
                coll.document(name)
                        .set(map)
                        .addOnSuccessListener(aVoid ->
                                Log.d(TAG, "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            }
            for (String name : missing) {
                coll.document(name).delete()
                        .addOnSuccessListener(v -> Log.v(TAG, name + " deleted"))
                        .addOnFailureListener(v -> Log.v(TAG, name + " delete failed"));
            }

            Map<String, Object> map = convertForUpload(context
                    .getSharedPreferences(prefName, Context.MODE_PRIVATE).getAll());
            userDoc.collection(fireData).document(prefName)
                    .set(map)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        });
    }

    private Map<String, Object> convertForUpload(Map<String, ?> map) {
        Map<String, Object> newMap = new HashMap<>(map);
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Set) {
                newMap.put(entry.getKey(), new ArrayList<Object>((Set) entry.getValue()));
            }
        }
        return newMap;
    }

    Task<Void> deleteAllDocuments(String uid) {
        DocumentReference userDoc = FirebaseFirestore.getInstance()
                .collection("users").document(uid);
        return userDoc.collection(fireTimetables).get().continueWithTask(task -> {
            QuerySnapshot documentSnapshots = task.getResult();
            assert documentSnapshots != null;
            List<Task<Void>> tasks = new ArrayList<>();
            tasks.add(userDoc.collection(fireData).document(prefName).delete());
            for (QueryDocumentSnapshot snapshot : documentSnapshots) {
                tasks.add(userDoc.collection(fireTimetables).document(snapshot.getId()).delete());
            }
            return Tasks.whenAll(tasks);
        });
    }
}
