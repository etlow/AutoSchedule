package teamget.autoschedule.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleModules {
    public static List<Module> getModules() {
        List<Module> list = new ArrayList<>();
        Location loc = new Location();

        Lesson a = new Lesson(1, 2, "Lecture", loc);
        Lesson b = new Lesson(11, 12, "Lecture", loc);
        Option o1 = new Option(Arrays.asList(a, b));

        Lesson c = new Lesson(2, 3, "Lecture", loc);
        Lesson d = new Lesson(12, 13, "Lecture", loc);
        Option o2 = new Option(Arrays.asList(c, d));

        List<Option> lec = Arrays.asList(o1, o2);

        Option ot1 = new Option(Arrays.asList(new Lesson(4, 5, "Tutorial", loc)));
        Option ot2 = new Option(Arrays.asList(new Lesson(6, 7, "Tutorial", loc)));

        List<Option> tut = Arrays.asList(ot1, ot2);

        Module testMod = new Module("ABC1231", Arrays.asList(lec, tut));

        return Arrays.asList(testMod);
    }
}
