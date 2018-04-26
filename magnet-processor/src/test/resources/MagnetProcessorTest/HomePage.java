package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Scope;
import magnet.Implementation;

@Implementation(type = Page.class)
class HomePage implements Page {

    HomePage(
            HomeRepository homeRepository,
            UserData userData,
            Scope scope
    ) { }

    @Override
    public void show() {
        // nop
    }

}