package app.extension;

import java.util.List;

import javax.annotation.Nullable;

import app.WorkProcessor;
import app.Page;
import magnet.Classifier;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageWithManyParameterizedWildcardKnownParams implements Page {

    HomePageWithManyParameterizedWildcardKnownParams(
            List<? extends WorkProcessor<Runnable>> variant1,
            @Classifier("global") List<? extends WorkProcessor<Runnable>> variant2,
            @Nullable List<? extends WorkProcessor<Runnable>> variant3,
            @Nullable @Classifier("global") List<? extends WorkProcessor<Runnable>> variant4
    ) { }

    @Override
    public void show() {
        // nop
    }

}
