package magnet.internal;

import app.Type10MagnetFactory;
import app.Type11MagnetFactory;
import app.Type12MagnetFactory;
import app.Type13MagnetFactory;
import app.Type14MagnetFactory;
import app.Type15MagnetFactory;
import app.Type16MagnetFactory;
import app.Type17MagnetFactory;
import app.Type18MagnetFactory;
import app.Type19MagnetFactory;
import app.Type1MagnetFactory;
import app.Type20MagnetFactory;
import app.Type2MagnetFactory;
import app.Type3MagnetFactory;
import app.Type4MagnetFactory;
import app.Type5MagnetFactory;
import app.Type6MagnetFactory;
import app.Type7MagnetFactory;
import app.Type8MagnetFactory;
import app.Type9MagnetFactory;
import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
                new Type1MagnetFactory(),
                new Type10MagnetFactory(),
                new Type11MagnetFactory(),
                new Type12MagnetFactory(),
                new Type13MagnetFactory(),
                new Type14MagnetFactory(),
                new Type15MagnetFactory(),
                new Type16MagnetFactory(),
                new Type17MagnetFactory(),
                new Type18MagnetFactory(),
                new Type19MagnetFactory(),
                new Type2MagnetFactory(),
                new Type20MagnetFactory(),
                new Type3MagnetFactory(),
                new Type4MagnetFactory(),
                new Type5MagnetFactory(),
                new Type6MagnetFactory(),
                new Type7MagnetFactory(),
                new Type8MagnetFactory(),
                new Type9MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(27);
        index.put(Type1MagnetFactory.getType(), new Range(0, 1, ""));
        index.put(Type10MagnetFactory.getType(), new Range(1, 1, ""));
        index.put(Type11MagnetFactory.getType(), new Range(2, 1, ""));
        index.put(Type12MagnetFactory.getType(), new Range(3, 1, ""));
        index.put(Type13MagnetFactory.getType(), new Range(4, 1, ""));
        index.put(Type14MagnetFactory.getType(), new Range(5, 1, ""));
        index.put(Type15MagnetFactory.getType(), new Range(6, 1, ""));
        index.put(Type16MagnetFactory.getType(), new Range(7, 1, ""));
        index.put(Type17MagnetFactory.getType(), new Range(8, 1, ""));
        index.put(Type18MagnetFactory.getType(), new Range(9, 1, ""));
        index.put(Type19MagnetFactory.getType(), new Range(10, 1, ""));
        index.put(Type2MagnetFactory.getType(), new Range(11, 1, ""));
        index.put(Type20MagnetFactory.getType(), new Range(12, 1, ""));
        index.put(Type3MagnetFactory.getType(), new Range(13, 1, ""));
        index.put(Type4MagnetFactory.getType(), new Range(14, 1, ""));
        index.put(Type5MagnetFactory.getType(), new Range(15, 1, ""));
        index.put(Type6MagnetFactory.getType(), new Range(16, 1, ""));
        index.put(Type7MagnetFactory.getType(), new Range(17, 1, ""));
        index.put(Type8MagnetFactory.getType(), new Range(18, 1, ""));
        index.put(Type9MagnetFactory.getType(), new Range(19, 1, ""));
        instanceManager.register(factories, index);
    }
}
