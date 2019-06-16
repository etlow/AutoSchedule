package teamget.autoschedule.mods;

import java.util.List;

public class Module {
    String code;
    List<List<Option>> list;

    public Module(String c, List<List<Option>> l) {
        code = c;
        list = l;
    }
}
