package app.extension;

import java.util.List;

import javax.annotation.Nullable;

import app.WorkProcessor;
import app.Page;
import magnet.Classifier;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageWithManyParameterizedWildcardInParams implements Page {

    HomePageWithManyParameterizedWildcardInParams(
            List<? extends WorkProcessor<? super Runnable>> variant1,
            @Classifier("global") List<? extends WorkProcessor<? super Runnable>> variant2,
            @Nullable List<? extends WorkProcessor<? super Runnable>> variant3,
            @Nullable @Classifier("global") List<? extends WorkProcessor<? super Runnable>> variant4
    ) { }

    @Override
    public void show() {
        // nop
    }

}
