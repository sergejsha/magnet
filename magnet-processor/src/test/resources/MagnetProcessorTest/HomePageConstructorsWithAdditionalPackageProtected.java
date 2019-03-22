package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class HomePageConstructorsWithAdditionalPackageProtected implements Page {

    HomePageConstructorsWithAdditionalPackageProtected(Scope scope) { }

    HomePageConstructorsWithAdditionalPackageProtected() { }

    @Override
    public void show() {
        // nop
    }

}
