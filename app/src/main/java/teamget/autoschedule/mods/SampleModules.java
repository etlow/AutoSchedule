package teamget.autoschedule.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleModules {
    public static List<List<Option>> genList() {
        Location loc0 = new Location("UTown");
        Location loc1 = new Location("FoS");
        Location loc2 = new Location("SoC");

        Lesson a = new Lesson(1, 2, "Lecture", loc0);
        Lesson b = new Lesson(11, 12, "Lecture", loc0);
        Option o1 = new Option(Arrays.asList(a, b));

        Lesson c = new Lesson(2, 3, "Lecture", loc1);
        Lesson d = new Lesson(12, 13, "Lecture", loc1);
        Option o2 = new Option(Arrays.asList(c, d));

        List<Option> lec = Arrays.asList(o1, o2);

        Option ot1 = new Option(Arrays.asList(new Lesson(4, 5, "Tutorial", loc2)));
        Option ot2 = new Option(Arrays.asList(new Lesson(6, 7, "Tutorial", loc2)));

        List<Option> tut = Arrays.asList(ot1, ot2);

        return Arrays.asList(lec, tut);
    }

    public static List<Module> getModules() {
        Module testMod1 = new Module("ABC1231", genList());
        Module testMod2 = new Module("DEF1010", genList());
        Module testMod3 = new Module("GHI2030", genList());
        Module testMod4 = new Module("JKL2040", genList());
        Module testMod5 = new Module("MNO2100", genList());
        Module testMod6 = new Module("PQR3230", genList());

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
