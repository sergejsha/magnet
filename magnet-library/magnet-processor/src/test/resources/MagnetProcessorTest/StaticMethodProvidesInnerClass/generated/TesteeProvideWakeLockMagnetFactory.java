package app;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class TesteeProvideWakeLockMagnetFactory extends InstanceFactory<PowerManager.WakeLock> {

    @Override
    public PowerManager.WakeLock create(Scope scope) {
        return Testee.provideWakeLock();
    }

    public static Class getType() {
        return PowerManager.WakeLock.class;
    }

}
