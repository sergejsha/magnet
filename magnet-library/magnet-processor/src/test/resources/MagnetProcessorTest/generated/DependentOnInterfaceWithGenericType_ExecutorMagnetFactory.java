package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class ExecutorMasterMagnetFactory extends InstanceFactory<ExecutorMaster> {
    @Override
    @SuppressWarnings("unchecked")
    public ExecutorMaster create(InstanceScope scope) {
        Executor executor = scope.getSingle(Executor.class);
        return new ExecutorMaster(executor);
    }

    public static Class getType() {
        return ExecutorMaster.class;
    }
}