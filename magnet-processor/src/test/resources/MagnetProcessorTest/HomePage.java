package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(forType = Page.class)
class HomePage implements Page {

    HomePage(
            HomeRepository homeRepository,
            UserData userData,
            DependencyScope dependencyScope
    ) { }

    @Override
    public void show() {
        // nop
    }

}