package app;

import magnet.Instance;

public class PowerManagerProvider {

    @Instance(type = PowerManager.WakeLock.class)
    public static PowerManager.WakeLock provideWakeLock() {
        return new PowerManager.WakeLock();
    }

}

