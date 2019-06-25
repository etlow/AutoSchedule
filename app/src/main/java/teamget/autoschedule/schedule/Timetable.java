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
    private int currPos = -1;
    private int numOptions = 0;
    private int[] pos;
    private Option[][] optionsArr;
    private Lesson[] table;

    private void setModules(List<Module> modList) {
        for (int i = 0; i < modList.size(); i++) {
            numOptions += modList.get(i).list.size();
        }
        pos = new int[numOptions];
        table = new Lesson[7 * 24 * numOptions];

        optionsArr = new Option[numOptions][];
        int nextPos = 0;
        for (int i = 0; i < modList.size(); i++) {
            List<List<Option>> list = modList.get(i).list;
            for (int j = 0; j < list.size(); j++) {
                List<Option> options = list.get(j);
                Option[] optArr = new Option[options.size()];
                optionsArr[nextPos++] = optArr;
                for (int k = 0; k < optArr.length; k++) {
                    optArr[k] = options.get(k);
                }
            }
        }
    }

    private boolean next() {
        boolean clash = false;
        do {
            if (currPos != -1) { // Not the first call
                // Keep removing elements from pos if it's already the last option
                while (pos[currPos] == optionsArr[currPos].length - 1) {
                    removeAll(optionsArr[currPos][pos[currPos]]);
                    if (currPos == 0) return false; // No more possibilities
                    currPos--;
                }
                // Update table to next option
                int optNum = pos[currPos]++;
                removeAll(optionsArr[currPos][optNum]);
                Option newOption = optionsArr[currPos][optNum + 1];
                // Check for clashes if newOption is added
                clash = clashes(newOption);
                addAll(newOption);
            }
            // Keep adding 0 to pos until last list is reached
            while (!clash && currPos < numOptions - 1) {
                currPos++;
                pos[currPos] = 0;
                Option newOption = optionsArr[currPos][0];
                clash = clashes(newOption);
                addAll(newOption);
            }
        } while (clash);
        return true;
    }

    private boolean clashes(Option option) {
        for (int i = 0; i < option.list.size(); i++) {
            Lesson newLesson = option.list.get(i);
            int start = newLesson.day * 24 + newLesson.startHour;
            int end = newLesson.day * 24 + newLesson.endHour;
            for (int j = start; j < end; j++) {
                for (int k = 0; k < numOptions; k++) {
                    Lesson lesson = table[j * numOptions + k];
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
                table[j * numOptions + currPos] = newLesson;
            }
        }
    }

    private void removeAll(Option option) {
        for (int i = 0; i < option.list.size(); i++) {
            Lesson newLesson = option.list.get(i);
            int start = newLesson.day * 24 + newLesson.startHour;
            int end = newLesson.day * 24 + newLesson.endHour;
            for (int j = start; j < end; j++) {
                table[j * numOptions + currPos] = null;
            }
        }
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
        t.setModules(mods);
        boolean hasNext = t.next();
        int i = 0;
        while (hasNext) {
            if (i % 1000000 == 0) {
                Log.v("Timetable", java.util.Arrays.toString(t.pos));
                Log.v("Timetable", t.toString());
            }
            hasNext = t.next();
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
