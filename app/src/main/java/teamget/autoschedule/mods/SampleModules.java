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
    private List<String> moduleCodes = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();

    public static List<String> getModuleCodes(Context context) {
        start(context);
        return instance.moduleCodes;
    }

    public static Module getModuleByCode(String code, Context context) {
        start(context);
        Module selected = null;
        for (Module module : instance.modules) {
            if (module.getCode().equals(code)) {
                selected = module;
            }
        }
        return selected;
    }

    public static void download(Context context) {
        if (instance == null) {
            instance = new SampleModules();
            instance.getModList(context);
        }
    }

    private static void start(Context context) {
        if (instance == null) {
            instance = new SampleModules();
            SharedPreferences modulesPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            for (Object data : modulesPref.getAll().values()) {
                instance.createMod(data.toString());
            }
            SharedPreferences listPref = context.getSharedPreferences("ModuleList", Context.MODE_PRIVATE);
            instance.createModList(listPref.getString("list", null));
        }
    }

    public static void download(String code, Context context) {
        start(context);
        if (!exists(code, instance.modules)) {
            instance.getModsTest(code, context);
        }
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
            createModList(result);
        }).execute("https://nusmods.com/api/v2/2018-2019/moduleList.json");
    }

    private void getModsTest(final String code, final Context context) {
        new DownloadTask(context, ex -> Log.v(TAG, ex.toString()), result -> {
            Log.v(TAG, result.substring(0, 1000));
            SharedPreferences modulesPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor modulesEditor = modulesPref.edit();
            modulesEditor.putString(code, result);
            modulesEditor.apply();
            createMod(result);
        }).execute("https://nusmods.com/api/v2/2018-2019/modules/" + code + ".json");
    }

    private void createModList(String result) {
        try {
            JSONArray jArr = new JSONArray(result);
            for (int i = 0; i < jArr.length(); i++) {
                moduleCodes.add(jArr.getJSONObject(i).getString("moduleCode"));
            }
        } catch (JSONException e) {
            Log.v("SampleModules", e.getMessage());
        }
    }

    private void createMod(String result) {
        try {
            JSONObject jObj = new JSONObject(result);
            String moduleCode = jObj.getString("moduleCode");
            JSONArray opts = jObj
                    .getJSONArray("semesterData")
                    .getJSONObject(1)
                    .getJSONArray("timetable");
            Map<String, Map<String, Option>> map = new HashMap<>();
            for (int i = 0; i < opts.length(); i++) {
                insertOption(map, opts.getJSONObject(i), moduleCode);
            }
            modules.add(new Module(moduleCode, toList(map)));
        } catch (JSONException e) {
            Log.v("SampleModules", e.getMessage());
        }
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
            option = new Option(moduleCode, new ArrayList<Lesson>());
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
