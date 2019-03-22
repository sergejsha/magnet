package app.extension;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class ExecutorMasterMagnetFactory extends InstanceFactory<ExecutorMaster> {
    @Override
    public ExecutorMaster create(Scope scope) {
        Executor<Runnable> executor = scope.getSingle(Executor.class, "");
        return new ExecutorMaster(executor);
    }

    public static Class getType() {
        return ExecutorMaster.class;
    }
}