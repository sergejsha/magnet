package app.extension;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class ExecutorImplMagnetFactory extends InstanceFactory<Executor> {
    @Override
    public Executor create(ScopeContainer scope) {
        return new ExecutorImpl();
    }

    public static Class getType() {
        return Executor.class;
    }
}