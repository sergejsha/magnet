package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class HomePageConstructorsWithNoPublicOrPackageProtected implements Page {

    protected HomePageConstructorsWithNoPublicOrPackageProtected(Scope scope) { }

    private HomePageConstructorsWithNoPublicOrPackageProtected() { }

    @Override
    public void show() {
        // nop
    }

}
