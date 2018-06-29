package app.extension;

import java.util.List;

import javax.annotation.Nullable;

import app.WorkProcessor;
import app.Page;
import magnet.Classifier;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageWithManyParameterizedWildcardParams<T extends Runnable> implements Page {

    HomePageWithManyParameterizedWildcardParams(
            List<? extends WorkProcessor<T>> variant1,
            @Classifier("global") List<? extends WorkProcessor<T>> variant2,
            @Nullable List<? extends WorkProcessor<T>> variant3,
            @Nullable @Classifier("global") List<? extends WorkProcessor<T>> variant4
    ) { }

    @Override
    public void show() {
        // nop
    }

}
