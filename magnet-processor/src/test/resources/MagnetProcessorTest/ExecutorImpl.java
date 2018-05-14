package app.extension;

import magnet.Instance;

@Instance(
        type = Executor.class
)
class ExecutorImpl implements Executor<Runnable> {

    @Override
    public void execute(Runnable runnable) { }

}