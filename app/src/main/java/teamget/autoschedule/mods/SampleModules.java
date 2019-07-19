package teamget.autoschedule.mods;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teamget.autoschedule.DownloadTask;

public class SampleModules {
    private static final String TAG = "SampleModules";
    private static SampleModules instance;
    private String currentYear;
    private int currentSem;
    private List<String> moduleCodes;
    private List<Module> modules = new ArrayList<>();
    private JSONArray modulesJSON;

    public static List<String> getModuleCodes(Context context) {
        start(context);
        return instance.moduleCodes;
    }

    public static List<Integer> getModuleSemesters(String code, Context context) {
        start(context);
        List<Integer> semesters = new ArrayList<>();
        JSONArray modules = instance.modulesJSON;
        for (int i = 0; i < modules.length(); i++) {
            try {
                JSONObject module = modules.getJSONObject(i);
                if (module.getString("moduleCode").equals(code)) {
                    JSONArray semestersJSON = module.getJSONArray("semesters");
                    for (int j = 0; j < semestersJSON.length(); j++) {
                        semesters.add(semestersJSON.getInt(j));
                    }
                    return semesters;
                }
            } catch (JSONException e) {
                Log.v("SampleModules", e.getMessage());
            }
        }
        return semesters;
    }

    public static Module getModuleByCode(int semester, String code, Context context) {
        start(context);
        if (semester != instance.currentSem) {
            instance.currentSem = semester;
            instance.modules = new ArrayList<>();
        }
        for (Module module : instance.modules) {
            if (module.getCode().equals(code)) {
                return module;
            }
        }
        return startMod(code, context);
    }

    public static void downloadModules(String year, Context context) {
        if (instance == null || !year.equals(instance.currentYear)) {
            instance = new SampleModules();
            instance.currentYear = year;
            instance.getModList(context);
        }
    }

    public static void download(String code, Context context) {
        start(context);
        if (!exists(code, instance.modules)) {
            instance.getModsTest(code, context);
        }
    }

    private static void start(Context context) {
        if (instance == null) {
            instance = new SampleModules();
            SharedPreferences listPref = context.getSharedPreferences("ModuleList", Context.MODE_PRIVATE);
            instance.moduleCodes = instance.createModList(listPref.getString("list", null));
        }
    }

    private static Module startMod(String code, Context context) {
        SharedPreferences modulesPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Module module = instance.createMod(modulesPref.getString(code, null));
        instance.modules.add(module);
        return module;
    }

    private static boolean exists(String code, List<Module> modules) {
        for (Module module : modules) {
            if (module.getCode().equals(code)) return true;
        }
        return false;
    }

    private void getModList(final Context context) {
        new DownloadTask(context, ex -> Log.v(TAG, ex.toString()), result -> {
            Log.v(TAG, result.substring(0, 300));
            SharedPreferences modulesPref = context.getSharedPreferences("ModuleList", Context.MODE_PRIVATE);
            SharedPreferences.Editor modulesEditor = modulesPref.edit();
            modulesEditor.putString("list", result);
            modulesEditor.apply();
            moduleCodes = createModList(result);
        }).execute("https://nusmods.com/api/v2/" + currentYear + "/moduleList.json");
    }

    private void getModsTest(final String code, final Context context) {
        new DownloadTask(context, ex -> Log.v(TAG, ex.toString()), result -> {
            Log.v(TAG, result.substring(0, 1000));
            SharedPreferences modulesPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor modulesEditor = modulesPref.edit();
            modulesEditor.putString(code, result);
            modulesEditor.apply();
            modules.add(createMod(result));
        }).execute("https://nusmods.com/api/v2/" + currentYear + "/modules/" + code + ".json");
    }

    private List<String> createModList(String result) {
        List<String> codes = new ArrayList<>();
        try {
            modulesJSON = new JSONArray(result);
            for (int i = 0; i < modulesJSON.length(); i++) {
                codes.add(modulesJSON.getJSONObject(i).getString("moduleCode"));
            }
        } catch (JSONException e) {
            Log.v("SampleModules", e.getMessage());
        }
        return codes;
    }

    private Module createMod(String result) {
        Module module = null;
        try {
            JSONObject jObj = new JSONObject(result);
            String moduleCode = jObj.getString("moduleCode");
            JSONArray semesterData = jObj.getJSONArray("semesterData");
            for (int i = 0; i < semesterData.length(); i++) {
                JSONObject semInfo = semesterData.getJSONObject(i);
                if (semInfo.getInt("semester") == currentSem) {
                    JSONArray opts = semInfo.getJSONArray("timetable");
                    Map<String, Map<String, Option>> map = new HashMap<>();
                    for (int j = 0; j < opts.length(); j++) {
                        insertOption(map, opts.getJSONObject(j), moduleCode);
                    }
                    module = new Module(moduleCode, toList(map));
                }
            }
        } catch (JSONException e) {
            Log.v("SampleModules", e.getMessage());
        }
        return module;
    }

    private void insertOption(Map<String, Map<String, Option>> map, JSONObject opt, String moduleCode)
            throws JSONException {
        String lessonType = opt.getString("lessonType");
        String classNo = opt.getString("classNo");
        List<Boolean> weekList = toWeekList(opt.getJSONArray("weeks"));
        boolean weeksSpecial = isSpecial(weekList);
        boolean oddWeeks = false;
        boolean evenWeeks = false;
        if (!weeksSpecial) {
            oddWeeks = weekList.get(0);
            evenWeeks = weekList.get(1);
        }
        Lesson lesson = new Lesson(
                opt.getString("day"),
                opt.getString("startTime"),
                opt.getString("endTime"),
                oddWeeks,
                evenWeeks,
                weeksSpecial,
                weekList,
                moduleCode,
                lessonType,
                new Location(opt.getString("venue")));
        Map<String, Option> options;
        if (map.containsKey(lessonType)) {
            options = map.get(lessonType);
        } else {
            options = new HashMap<>();
            map.put(lessonType, options);
        }
        assert options != null;

        Option option;
        if (options.containsKey(classNo)) {
            option = options.get(classNo);
        } else {
            option = new Option(moduleCode, new ArrayList<>());
            options.put(classNo, option);
        }
        assert option != null;
        option.list.add(lesson);
    }

    private static List<Boolean> toWeekList(JSONArray jWeeks) throws JSONException {
        List<Boolean> weekList = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            weekList.add(false);
        }
        for (int i = 0; i < jWeeks.length(); i++) {
            weekList.set(jWeeks.getInt(i) - 1, true);
        }
        return weekList;
    }

    private static boolean isSpecial(List<Boolean> weekList) {
        boolean firstWeek = weekList.get(0);
        boolean secondWeek = weekList.get(1);
        for (int i = 2; i < 13; i += 2) if (weekList.get(i) != firstWeek) return true;
        for (int i = 3; i < 13; i += 2) if (weekList.get(i) != secondWeek) return true;
        return false;
    }

    private List<List<Option>> toList(Map<String, Map<String, Option>> map) {
        List<List<Option>> list = new ArrayList<>();
        for (Map<String, Option> optMap : map.values()) {
            list.add(new ArrayList<>(optMap.values()));
        }
        return list;
    }
}
