package magnet.internal;

import java.util.AbstractList;

/** Subject to change. For internal use only. */
public final class Dependencies extends AbstractList<String> {

    private final String[] dependencies;

    public Dependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String get(int i) {
        if (i < 0 || i >= dependencies.length) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return dependencies[i];
    }

    @Override
    public int size() {
        return dependencies.length;
    }

    public static String key(Class<?> type, String classifier) {
        if (classifier == null || classifier.length() == 0) {
            return type.getName();
        }
        return classifier + "@" + type.getName();
    }

}
