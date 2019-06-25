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
    private int numEvents = 0;
    private int[] pos;
    private Event[][][] eventsArr;
    private Event[] table;

    private void setModules(List<Module> modList) {
        for (int i = 0; i < modList.size(); i++) {
            numEvents += modList.get(i).list.size();
        }
        pos = new int[numEvents];
        table = new Event[7 * 24 * numEvents];

        eventsArr = new Event[numEvents][][];
        int nextPos = 0;
        for (int i = 0; i < modList.size(); i++) {
            List<List<Option>> list = modList.get(i).list;
            for (int j = 0; j < list.size(); j++) {
                eventsArr[nextPos++] = toEventArr(list.get(j));
            }
        }
    }

    private static <E> E[] addToArr(E[] arr, List<E> list) {
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private static Event[][] toEventArr(List<Option> opts) {
        List<Event[]> events = new ArrayList<>();
        List<Option> options = new ArrayList<>(opts);
        while (!options.isEmpty()) {
            Option option = options.remove(options.size() - 1);
            List<Option> sameTimeOptions = new ArrayList<>();
            sameTimeOptions.add(option);
            for (int k = options.size() - 1; k >= 0; k--) {
                if (sameTime(option, options.get(k))) {
                    sameTimeOptions.add(options.remove(k));
                }
            }
            Event[] eventArr = new Event[option.list.size()];
            for (int i = 0; i < eventArr.length; i++) {
                eventArr[i] = new Event(option.list.get(i), sameTimeOptions);
            }
            events.add(eventArr);
        }
        return addToArr(new Event[events.size()][], events);
    }

    private static boolean sameTime(Option a, Option b) {
        List<Lesson> aList = new ArrayList<>(a.list);
        for (Lesson lesson : b.list) {
            boolean found = false;
            for (int i = 0; i < aList.size() && !found; i++) {
                if (sameTime(aList.get(i), lesson)) {
                    aList.remove(i);
                    found = true;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean sameTime(Lesson a, Lesson b) {
        return a.day == b.day && a.startHour == b.startHour && a.endHour == b.endHour
                && a.oddWeek == b.oddWeek && a.evenWeek == b.evenWeek;
    }

    private boolean next() {
        boolean clash = false;
        do {
            if (currPos != -1) { // Not the first call
                // Keep removing elements from pos if it's already the last option
                while (pos[currPos] == eventsArr[currPos].length - 1) {
                    removeAll(eventsArr[currPos][pos[currPos]]);
                    if (currPos == 0) return false; // No more possibilities
                    currPos--;
                }
                // Update table to next option
                int optNum = pos[currPos]++;
                removeAll(eventsArr[currPos][optNum]);
                Event[] newOption = eventsArr[currPos][optNum + 1];
                // Check for clashes if newOption is added
                clash = clashes(newOption);
                addAll(newOption);
            }
            // Keep adding 0 to pos until last list is reached
            while (!clash && currPos < numEvents - 1) {
                currPos++;
                pos[currPos] = 0;
                Event[] newOption = eventsArr[currPos][0];
                clash = clashes(newOption);
                addAll(newOption);
            }
        } while (clash);
        return true;
    }

    private boolean clashes(Event[] eventArr) {
        for (Event newEvent : eventArr) {
            int start = newEvent.day * 24 + newEvent.startHour;
            int end = newEvent.day * 24 + newEvent.endHour;
            for (int j = start; j < end; j++) {
                for (int k = 0; k < numEvents; k++) {
                    Event event = table[j * numEvents + k];
                    if (event != null && newEvent.overlaps(event)) return true;
                }
            }
        }
        return false;
    }

    private void addAll(Event[] eventArr) {
        for (Event newEvent : eventArr) {
            int start = newEvent.day * 24 + newEvent.startHour;
            int end = newEvent.day * 24 + newEvent.endHour;
            for (int j = start; j < end; j++) {
                table[j * numEvents + currPos] = newEvent;
            }
        }
    }

    private void removeAll(Event[] eventArr) {
        for (Event newEvent : eventArr) {
            int start = newEvent.day * 24 + newEvent.startHour;
            int end = newEvent.day * 24 + newEvent.endHour;
            for (int j = start; j < end; j++) {
                table[j * numEvents + currPos] = null;
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
        for (Event l : table) if (l != null) {
            stringBuilder.append(l.startHour);
            stringBuilder.append("-");
            stringBuilder.append(l.endHour);
            stringBuilder.append(l.options.get(0).list.get(0).moduleCode);
            stringBuilder.append(l.options.get(0).list.get(0).type);
            stringBuilder.append(l.options.get(0).list.get(0).location);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
