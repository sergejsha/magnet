package app.extension;

import javax.annotation.Nullable;
import javax.inject.Named;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Implementation;

@Implementation(forType = Page.class)
class HomePageWithNamedParams implements Page {

    private static final String LOCAL = "local";

    HomePageWithNamedParams(
            @Nullable @Named(LOCAL) HomeRepository homeRepository,
            @Named("global") UserData userData
    ) { }

    @Override
    public void show() {
        // nop
    }

}