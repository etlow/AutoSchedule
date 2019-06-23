package teamget.autoschedule.mods;

import java.util.List;

public class Module {
    private String code;
    public List<List<Option>> list;

    public Module(String c, List<List<Option>> l) {
        code = c;
        list = l;
    }

    public String getCode() { return code; }

    @Override
    public String toString() {
        return code;
    }
}
