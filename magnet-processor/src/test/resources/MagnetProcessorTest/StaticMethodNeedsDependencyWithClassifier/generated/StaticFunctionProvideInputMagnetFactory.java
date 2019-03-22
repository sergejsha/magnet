package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class StaticFunctionProvideInputMagnetFactory extends InstanceFactory<Output> {

    @Override
    public Output create(Scope scope) {
        Input input = scope.getSingle(Input.class, "application");
        return StaticFunction.provide(input);
    }

    public static Class getType() {
        return Output.class;
    }

}