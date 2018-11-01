package app;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class TesteeProvideWakeLockMagnetFactory extends InstanceFactory<PowerManager.WakeLock> {

    @Override
    public PowerManager.WakeLock create(InstanceScope scope) {
        return Testee.provideWakeLock();
    }

    public static Class getType() {
        return PowerManager.WakeLock.class;
    }

}
