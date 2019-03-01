package app;

import magnet.Instance;
import magnet.Classifier;

public class PowerManagerProvider {

    @Instance(type = PowerManager.WakeLock.class)
    public static PowerManager.WakeLock provideWakeLock(@Classifier(Context.APPLICATION) Context context) {
        return new PowerManager.WakeLock();
    }

}

