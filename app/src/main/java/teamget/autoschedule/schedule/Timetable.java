package teamget.autoschedule.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.mods.Module;
import teamget.autoschedule.mods.Option;
import teamget.autoschedule.mods.SampleModules;

public class Timetable {
    private int currModule = 0;
    private int currList = -1;
    private List<Integer> pos = new ArrayList<>();
    private List<Lesson> table = new ArrayList<>();
    public boolean next(List<Module> modules) {
        if (currList != -1) {
            while (pos.get(pos.size() - 1) == modules.get(currModule).list.get(currList).size() - 1) {
                int optNum = pos.remove(pos.size() - 1);
                table.removeAll(modules.get(currModule).list.get(currList).get(optNum).list);
                if (currList == 0) {
                    if (currModule == 0) {
                        return false;
                    }
                    currModule--;
                    currList = modules.get(currModule).list.size();
                }
                currList--;
            }
            int optNum = pos.get(pos.size() - 1);
            pos.set(pos.size() - 1, optNum + 1);
            table.removeAll(modules.get(currModule).list.get(currList).get(optNum).list);
            table.addAll(modules.get(currModule).list.get(currList).get(optNum + 1).list);
        }
        boolean last = false;
        while (!last) {
            List<List<Option>> typeList = modules.get(currModule).list;
            while (currList < typeList.size() - 1) {
                pos.add(0);
                currList++;
                table.addAll(typeList.get(currList).get(0).list);
            }
            if (currModule == modules.size() - 1) {
                last = true;
            } else {
                currModule++;
                currList = -1;
            }
        }
        return true;
    }

    private static List<Module> getAndClearModules(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                "ModulePreferences", Context.MODE_PRIVATE);
        Set<String> modSet = sharedPref.getStringSet("modules", Collections.<String>emptySet());

        List<Module> mods = new ArrayList<>();
        for (String modCode : modSet) {
            mods.add(SampleModules.getModuleByCode(modCode));
        }
        sharedPref.edit().clear().apply();
        return mods;
    }

    public static void test(Context context) {
        List<Module> mods = getAndClearModules(context);
        Log.v("Timetable", mods.toString());
        Timetable t = new Timetable();
        boolean hasNext = t.next(mods);
        while (hasNext) {
            Log.v("Timetable", t.toString());
            hasNext = t.next(mods);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Lesson l : table) {
            stringBuilder.append(l.startHour);
            stringBuilder.append("-");
            stringBuilder.append(l.endHour);
            stringBuilder.append(l.moduleCode);
            stringBuilder.append(l.type);
            stringBuilder.append(l.location);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
