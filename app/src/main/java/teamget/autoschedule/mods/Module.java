package teamget.autoschedule.mods;

import java.util.List;

public class Module {
    public String code;
    public List<List<Option>> list;

    public Module(String c, List<List<Option>> l) {
        code = c;
        list = l;
    }

    @Override
    public String toString() {
        return code;
    }
}
