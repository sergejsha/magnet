package app;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class TesteeProvideWakeLockMagnetFactory extends InstanceFactory<PowerManager.WakeLock> {

    @Override
    public PowerManager.WakeLock create(ScopeContainer scope) {
        return Testee.provideWakeLock();
    }

    public static Class getType() {
        return PowerManager.WakeLock.class;
    }

}
