package app;

import magnet.Instance;

public class Context {
    public static final String APPLICATION = "application";
    public static final String ACTIVITY = "activity";


    @Instance(type = Context.class, classifier = APPLICATION)
    public static Context provideApplicationContext() {
        return new Context();
    }

    @Instance(type = Context.class, classifier = ACTIVITY)
    public static Context provideActivityContext() {
        return new Context();
    }
}
