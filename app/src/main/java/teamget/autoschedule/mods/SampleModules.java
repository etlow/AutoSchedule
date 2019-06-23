package teamget.autoschedule.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleModules {
    public static Module genModule(String moduleCode, int day) {
        Location loc0 = new Location("UTown");
        Location loc1 = new Location("FoS");
        Location loc2 = new Location("SoC");

        Lesson a = new Lesson(day, 1, 2, true, true, moduleCode, "Lecture", loc0);
        Lesson b = new Lesson(day, 11, 12, true, true, moduleCode, "Lecture", loc0);
        Option o1 = new Option(Arrays.asList(a, b));

        Lesson c = new Lesson(day, 2, 3, true, true, moduleCode, "Lecture", loc1);
        Lesson d = new Lesson(day, 12, 13, true, true, moduleCode, "Lecture", loc1);
        Option o2 = new Option(Arrays.asList(c, d));

        List<Option> lec = Arrays.asList(o1, o2);

        Option ot1 = new Option(Arrays.asList(new Lesson(day, 4, 5, true, true, moduleCode, "Tutorial", loc2)));
        Option ot2 = new Option(Arrays.asList(new Lesson(day, 6, 7, true, true, moduleCode, "Tutorial", loc2)));

        List<Option> tut = Arrays.asList(ot1, ot2);

        return new Module(moduleCode, Arrays.asList(lec, tut));
    }

    public static List<Module> getModules() {
        Module testMod1 = genModule("ABC1231", 1);
        Module testMod2 = genModule("DEF1010", 1);
        Module testMod3 = genModule("GHI2030", 1);
        Module testMod4 = genModule("JKL2040", 1);
        Module testMod5 = genModule("MNO2100", 2);
        Module testMod6 = genModule("PQR3230", 2);

        return Arrays.asList(testMod1, testMod2, testMod3, testMod4, testMod5, testMod6);
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
