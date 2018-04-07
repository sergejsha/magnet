package app.extension;

import app.Page;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(type = Page.class)
class HomePageWithDependencyScope implements Page {

    HomePageWithDependencyScope(DependencyScope dependencyScope) { }

    @Override
    public void show() {
        // nop
    }

}