package test;

import java.util.List;
import magnet.Classifier;
import magnet.internal.InstanceScope;

final class MagnetInstanceScope1 extends InstanceScope implements Scope1 {
    public MagnetInstanceScope1() {
        super(false);
    }

    @Override
    public String getName1() {
        return getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String getName2() {
        return getSingle(String.class, "name2");
    }

    @Override
    public String getName3() {
        return getOptional(String.class, Classifier.NONE);
    }

    @Override
    public String getName4() {
        return getOptional(String.class, "name4");
    }

    @Override
    public List<String> getName5() {
        return getMany(String.class, Classifier.NONE);
    }

    @Override
    public List<String> getName6() {
        return getMany(String.class, "name6");
    }
}