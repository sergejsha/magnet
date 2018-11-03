package app;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class TesteeProvideWakeLockMagnetFactory extends InstanceFactory<PowerManager.WakeLock> {

    @Override
    public PowerManager.WakeLock create(ScopeContainer scope) {
        return Testee.provideWakeLock();
    }

    public static Class getType() {
        return PowerManager.WakeLock.class;
    }

}
