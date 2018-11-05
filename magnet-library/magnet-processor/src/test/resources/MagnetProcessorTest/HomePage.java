package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Instance;
import magnet.ScopeContainer;

@Instance(type = Page.class)
class HomePage implements Page {

    HomePage(
            HomeRepository homeRepository,
            UserData userData,
            ScopeContainer scope
    ) { }

    @Override
    public void show() {
        // nop
    }

}