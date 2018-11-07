package app.extension;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class ExecutorMasterMagnetFactory extends InstanceFactory<ExecutorMaster> {
    @Override
    @SuppressWarnings("unchecked")
    public ExecutorMaster create(Scope scope) {
        Executor executor = scope.getSingle(Executor.class);
        return new ExecutorMaster(executor);
    }

    public static Class getType() {
        return ExecutorMaster.class;
    }
}