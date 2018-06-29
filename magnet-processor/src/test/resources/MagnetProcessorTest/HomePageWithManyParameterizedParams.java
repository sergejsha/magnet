package app.extension;

import java.util.List;

import javax.annotation.Nullable;

import app.WorkProcessor;
import app.Page;
import magnet.Classifier;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageWithManyParameterizedParams<T extends Runnable> implements Page {

    HomePageWithManyParameterizedParams(
            List<WorkProcessor<T>> variant1,
            @Classifier("global") List<WorkProcessor<T>> variant2,
            @Nullable List<WorkProcessor<T>> variant3,
            @Nullable @Classifier("global") List<WorkProcessor<T>> variant4
    ) { }

    @Override
    public void show() {
        // nop
    }

}
