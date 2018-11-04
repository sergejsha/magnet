package app.extension;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class ExecutorMasterMagnetFactory extends InstanceFactory<ExecutorMaster> {
    @Override
    @SuppressWarnings("unchecked")
    public ExecutorMaster create(ScopeContainer scope) {
        Executor executor = scope.getSingle(Executor.class);
        return new ExecutorMaster(executor);
    }

    public static Class getType() {
        return ExecutorMaster.class;
    }
}