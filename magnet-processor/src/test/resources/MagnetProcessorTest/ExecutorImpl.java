package app.extension;

import magnet.Implementation;

@Implementation(
        forType = Executor.class
)
class ExecutorImpl implements Executor<Runnable> {

    @Override
    public void execute(Runnable runnable) { }

}