package app.extension;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Implementation;

@Implementation(forType = Page.class)
class HomePageWithParams implements Page {

    HomePageWithParams(
            @Nullable HomeRepository homeRepository,
            @NotNull UserData userData
    ) { }

    @Override
    public void show() {
        // nop
    }

}