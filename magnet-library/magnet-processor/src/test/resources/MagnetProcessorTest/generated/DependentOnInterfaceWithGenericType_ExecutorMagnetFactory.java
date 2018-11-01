package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

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