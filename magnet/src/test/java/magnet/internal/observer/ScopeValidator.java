package magnet.internal.observer;

public interface ScopeValidator {
    void hasNoInstances();
    void hasInstanceTypes(Class<?>... instanceTypes);
}
