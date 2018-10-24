package selector;

import magnet.SelectorFilter;

public class SimpleSelectorFilter extends SelectorFilter {

    public String getId() { return "simple"; }
    public boolean filter(String[] selector) { return true; }

}