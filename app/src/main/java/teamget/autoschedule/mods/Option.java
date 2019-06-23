package teamget.autoschedule.mods;

import java.util.List;

public class Option {
    public String classNo;
    public List<Lesson> list;

    public Option(String c, List<Lesson> l) {
        classNo = c;
        list = l;
    }
}
