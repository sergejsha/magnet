package app.extension;

import magnet.Instance;

@Instance(type = ExecutorMaster.class)
class ExecutorMaster {

    public ExecutorMaster(Executor<Runnable> executor) {}

}