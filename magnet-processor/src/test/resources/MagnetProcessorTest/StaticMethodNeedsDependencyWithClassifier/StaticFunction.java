package app;

import magnet.Classifier;
import magnet.Instance;

public class StaticFunction {

    @Instance(type = Output.class)
    public static Output provide(@Classifier(Constants.APPLICATION) Input input) {
        return new Output();
    }

}