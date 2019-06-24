package teamget.autoschedule.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
    private int numLessons = 0;
    private List<Integer> pos = new ArrayList<>();
    private Lesson[] table;
    private boolean next(List<Module> modules) {
        boolean clash = true;
        while (clash) {
            clash = false;
            if (currList != -1) { // Not the first call
                // Keep removing elements from pos if it's already the last option
                while (pos.get(pos.size() - 1) == modules.get(currModule).list.get(currList).size() - 1) {
                    int optNum = pos.get(pos.size() - 1);
                    removeAll(modules.get(currModule).list.get(currList).get(optNum));
                    pos.remove(pos.size() - 1);
                    if (currList == 0) {
                        if (currModule == 0) {
                            // No more possibilities
                            return false;
                        }
                        currModule--;
                        currList = modules.get(currModule).list.size();
                    }
                    currList--;
                }
                // Update table to next possibility
                int optNum = pos.get(pos.size() - 1);
                pos.set(pos.size() - 1, optNum + 1);
                removeAll(modules.get(currModule).list.get(currList).get(optNum));
                Option newOption = modules.get(currModule).list.get(currList).get(optNum + 1);
                // Check for clashes if newOption is added
                clash = clashes(newOption);
                addAll(newOption);
            }
            boolean last = false;
            // Keep adding 0 to pos until last list of last module is reached
            while (!clash && !last) {
                List<List<Option>> typeList = modules.get(currModule).list;
                while (!clash && currList < typeList.size() - 1) {
                    pos.add(0);
                    currList++;
                    Option newOption = typeList.get(currList).get(0);
                    clash = clashes(newOption);
                    addAll(newOption);
                }
                if (!clash) {
                    if (currModule == modules.size() - 1) {
                        // Last list of last module
                        last = true;
                    } else {
                        currModule++;
                        currList = -1;
                    }
                }
            }
        }
        return true;
    }

    private boolean clashes(Option option) {
        for (int i = 0; i < option.list.size(); i++) {
            Lesson newLesson = option.list.get(i);
            int start = newLesson.day * 24 + newLesson.startHour;
            int end = newLesson.day * 24 + newLesson.endHour;
            for (int j = start; j < end; j++) {
                for (int k = 0; k < numLessons; k++) {
                    Lesson lesson = table[j * numLessons + k];
                    if (lesson != null && newLesson.overlaps(lesson)) return true;
                }
            }
        }
        return false;
    }

    private void addAll(Option option) {
        for (int i = 0; i < option.list.size(); i++) {
            Lesson newLesson = option.list.get(i);
            int start = newLesson.day * 24 + newLesson.startHour;
            int end = newLesson.day * 24 + newLesson.endHour;
            for (int j = start; j < end; j++) {
                table[j * numLessons + pos.size() - 1] = newLesson;
            }
        }
    }

    private void removeAll(Option option) {
        if (pos.isEmpty()) return;
        for (int i = 0; i < option.list.size(); i++) {
            Lesson newLesson = option.list.get(i);
            int start = newLesson.day * 24 + newLesson.startHour;
            int end = newLesson.day * 24 + newLesson.endHour;
            for (int j = start; j < end; j++) {
                table[j * numLessons + pos.size() - 1] = null;
            }
        }
    }

    private void createTable(List<Module> modules) {
        for (int i = 0; i < modules.size(); i++) {
            numLessons += modules.get(i).list.size();
        }
        table = new Lesson[7 * 24 * numLessons];
    }

    private static List<Module> getAndClearModules(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                "ModulePreferences", Context.MODE_PRIVATE);
        Set<String> modSet = sharedPref.getStringSet("modules", Collections.<String>emptySet());

        List<Module> mods = new ArrayList<>();
        for (String modCode : modSet) {
            mods.add(SampleModules.getModuleByCode(modCode));
        }
        sharedPref.edit().remove("modules").apply();
        return mods;
    }

    public static void test(Context context) {
        List<Module> mods = getAndClearModules(context);
        Log.v("Timetable", mods.toString());
        if (mods.size() == 0) return;
        Timetable t = new Timetable();
        t.createTable(mods);
        boolean hasNext = t.next(mods);
        int i = 0;
        while (hasNext) {
            if (i % 1000000 == 0) {
                Log.v("Timetable", t.pos.toString());
                Log.v("Timetable", t.toString());
            }
            hasNext = t.next(mods);
            i++;
        }
        Log.v("Timetable", "Count: " + i);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Lesson l : table) if (l != null) {
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
