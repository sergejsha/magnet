package app.extension;

import javax.annotation.Nullable;

import app.HomeRepository;
import app.Page;
import java.util.List;

import magnet.Classifier;
import magnet.Implementation;


@Implementation(type = Page.class)
class HomePageWithManyParams implements Page {

    HomePageWithManyParams(
            List<HomeRepository> variant1,
            @Classifier("global") List<HomeRepository> variant2,
            @Nullable List<HomeRepository> variant3,
            @Nullable @Classifier("global") List<HomeRepository> variant4
    ) { }

    @Override
    public void show() {
        // nop
    }

}