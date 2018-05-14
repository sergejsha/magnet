package app.extension;

import javax.annotation.Nullable;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Instance;
import magnet.Classifier;

@Instance(type = Page.class)
class HomePageWithClassifierParams implements Page {

    private static final String LOCAL = "local";

    HomePageWithClassifierParams(
            @Nullable @Classifier(LOCAL) HomeRepository homeRepository,
            @Classifier("global") UserData userData
    ) { }

    @Override
    public void show() {
        // nop
    }

}