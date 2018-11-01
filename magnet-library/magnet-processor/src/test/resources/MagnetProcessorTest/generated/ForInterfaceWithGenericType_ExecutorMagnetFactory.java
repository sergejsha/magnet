package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class ExecutorImplMagnetFactory extends InstanceFactory<Executor> {
    @Override
    public Executor create(InstanceScope scope) {
        return new ExecutorImpl();
    }

    public static Class getType() {
        return Executor.class;
    }
}