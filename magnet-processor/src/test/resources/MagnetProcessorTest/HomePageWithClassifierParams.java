package app.extension;

import javax.annotation.Nullable;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.internal.InstanceFactory;
import magnet.Implementation;
import magnet.Classifier;
import magnet.Scope;

@Implementation(type = Page.class)
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