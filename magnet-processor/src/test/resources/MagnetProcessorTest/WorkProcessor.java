package app;

public interface WorkProcessor<I extends Runnable> {

    void processWork(I processor);

}
