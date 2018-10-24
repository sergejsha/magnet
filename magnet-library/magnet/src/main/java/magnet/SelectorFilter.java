package magnet;

public abstract class SelectorFilter {

    public static final String DEFAULT_SELECTOR = "";

    public abstract String getId();
    public abstract boolean filter(String[] selector);
}
