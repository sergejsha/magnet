package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class PowerManagerProviderProvideWakeLockMagnetFactory extends InstanceFactory<PowerManager.WakeLock> {

    @Override
    public PowerManager.WakeLock create(Scope scope) {
        Context context = scope.getSingle(Context.class, "application");
        return PowerManagerProvider.provideWakeLock(context);
    }

    public static Class getType() {
        return PowerManager.WakeLock.class;
    }

}
