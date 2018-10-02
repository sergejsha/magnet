package app.extension;

interface Executor<R extends Runnable> {

    void execute(R runnable);

}