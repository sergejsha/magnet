package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Instance;
import magnet.internal.InstanceScope;

@Instance(type = Page.class)
class HomePage implements Page {

    HomePage(
            HomeRepository homeRepository,
            UserData userData,
            InstanceScope scope
    ) { }

    @Override
    public void show() {
        // nop
    }

}