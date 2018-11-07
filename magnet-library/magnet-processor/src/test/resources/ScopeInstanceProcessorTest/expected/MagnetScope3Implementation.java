package test;

import magnet.Scope;
import magnet.internal.InternalFactory;

final class MagnetScope3Implementation implements Scope3 {
    private final Scope scopeContainer;
    public MagnetScope3Implementation(Scope scopeContainer) {
        this.scopeContainer = scopeContainer;
    }

    @Override
    public <T> T createSubscope(Class<T> scopeType) {
        return InternalFactory.createScope(scopeType, scopeContainer.createSubscope());
    }

}