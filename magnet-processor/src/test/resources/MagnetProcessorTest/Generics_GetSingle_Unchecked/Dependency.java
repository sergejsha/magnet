package app;

interface Dependency<T extends Runnable> {
    void run();
}