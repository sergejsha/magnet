package test;

import magnet.ScopeContainer;
import magnet.internal.InternalFactory;

final class MagnetScope3Implementation implements Scope3 {
    private final ScopeContainer scopeContainer;
    public MagnetScope3Implementation(ScopeContainer scopeContainer) {
        this.scopeContainer = scopeContainer;
    }

    @Override
    public <T> T createSubscope(Class<T> scopeType) {
        return InternalFactory.createScope(scopeType, scopeContainer.createSubscope());
    }

}