package magnet.internal;

import app.ManyTypesType10MagnetFactory;
import app.ManyTypesType11MagnetFactory;
import app.ManyTypesType12MagnetFactory;
import app.ManyTypesType13MagnetFactory;
import app.ManyTypesType14MagnetFactory;
import app.ManyTypesType15MagnetFactory;
import app.ManyTypesType16MagnetFactory;
import app.ManyTypesType17MagnetFactory;
import app.ManyTypesType18MagnetFactory;
import app.ManyTypesType19MagnetFactory;
import app.ManyTypesType1MagnetFactory;
import app.ManyTypesType20MagnetFactory;
import app.ManyTypesType2MagnetFactory;
import app.ManyTypesType3MagnetFactory;
import app.ManyTypesType4MagnetFactory;
import app.ManyTypesType5MagnetFactory;
import app.ManyTypesType6MagnetFactory;
import app.ManyTypesType7MagnetFactory;
import app.ManyTypesType8MagnetFactory;
import app.ManyTypesType9MagnetFactory;
import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new ManyTypesType1MagnetFactory(),
            new ManyTypesType10MagnetFactory(),
            new ManyTypesType11MagnetFactory(),
            new ManyTypesType12MagnetFactory(),
            new ManyTypesType13MagnetFactory(),
            new ManyTypesType14MagnetFactory(),
            new ManyTypesType15MagnetFactory(),
            new ManyTypesType16MagnetFactory(),
            new ManyTypesType17MagnetFactory(),
            new ManyTypesType18MagnetFactory(),
            new ManyTypesType19MagnetFactory(),
            new ManyTypesType2MagnetFactory(),
            new ManyTypesType20MagnetFactory(),
            new ManyTypesType3MagnetFactory(),
            new ManyTypesType4MagnetFactory(),
            new ManyTypesType5MagnetFactory(),
            new ManyTypesType6MagnetFactory(),
            new ManyTypesType7MagnetFactory(),
            new ManyTypesType8MagnetFactory(),
            new ManyTypesType9MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(27);
        index.put(ManyTypesType1MagnetFactory.getType(), new Range(0, 1, ""));
        index.put(ManyTypesType10MagnetFactory.getType(), new Range(1, 1, ""));
        index.put(ManyTypesType11MagnetFactory.getType(), new Range(2, 1, ""));
        index.put(ManyTypesType12MagnetFactory.getType(), new Range(3, 1, ""));
        index.put(ManyTypesType13MagnetFactory.getType(), new Range(4, 1, ""));
        index.put(ManyTypesType14MagnetFactory.getType(), new Range(5, 1, ""));
        index.put(ManyTypesType15MagnetFactory.getType(), new Range(6, 1, ""));
        index.put(ManyTypesType16MagnetFactory.getType(), new Range(7, 1, ""));
        index.put(ManyTypesType17MagnetFactory.getType(), new Range(8, 1, ""));
        index.put(ManyTypesType18MagnetFactory.getType(), new Range(9, 1, ""));
        index.put(ManyTypesType19MagnetFactory.getType(), new Range(10, 1, ""));
        index.put(ManyTypesType2MagnetFactory.getType(), new Range(11, 1, ""));
        index.put(ManyTypesType20MagnetFactory.getType(), new Range(12, 1, ""));
        index.put(ManyTypesType3MagnetFactory.getType(), new Range(13, 1, ""));
        index.put(ManyTypesType4MagnetFactory.getType(), new Range(14, 1, ""));
        index.put(ManyTypesType5MagnetFactory.getType(), new Range(15, 1, ""));
        index.put(ManyTypesType6MagnetFactory.getType(), new Range(16, 1, ""));
        index.put(ManyTypesType7MagnetFactory.getType(), new Range(17, 1, ""));
        index.put(ManyTypesType8MagnetFactory.getType(), new Range(18, 1, ""));
        index.put(ManyTypesType9MagnetFactory.getType(), new Range(19, 1, ""));
        instanceManager.register(factories, index, null);
    }
}