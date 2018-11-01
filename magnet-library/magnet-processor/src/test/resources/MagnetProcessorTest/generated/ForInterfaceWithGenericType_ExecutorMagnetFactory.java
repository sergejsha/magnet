package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class ExecutorImplMagnetFactory extends InstanceFactory<Executor> {
    @Override
    public Executor create(ScopeContainer scope) {
        return new ExecutorImpl();
    }

    public static Class getType() {
        return Executor.class;
    }
}