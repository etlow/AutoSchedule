package teamget.autoschedule.mods;

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
    private static SampleModules instance;
    private List<Module> modules = new ArrayList<>();

    public static List<Module> getModules() {
        return instance.modules;
    }

    public static void download() {
        if (instance == null) {
            instance = new SampleModules();
            instance.getModsTest("CS2030");
            instance.getModsTest("CS2040");
            instance.getModsTest("GER1000");
            instance.getModsTest("GEQ1000");
        }
    }

    private void getModsTest(String code) {
        new DownloadTask(new DownloadTask.Callback() {
            @Override
            public void call(String result) {
                Log.v("SampleModules", result);
                createMod(result);
            }
        }).execute("https://nusmods.com/api/v2/2018-2019/modules/" + code + ".json");
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

    public static Module getModuleByCode(String code) {
        Module selected = null;
        for (Module module : getModules()) {
            if (module.getCode().equals(code)) {
                selected = module;
            }
        }
        return selected;
    }
}
