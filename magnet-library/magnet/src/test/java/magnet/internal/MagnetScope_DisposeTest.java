package magnet.internal;

import magnet.Classifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetScope_DisposeTest {

    @Mock private InstanceManager instanceManager;
    @Mock private Factory1 factory1;
    @Mock private Factory2 factory2;
    @Mock private Factory3 factory3;
    @Mock private Interface1 instance1;
    @Mock private Interface1 instance2;
    @Mock private Interface1 instance3;

    @Test
    public void disposeSingleScope_NoClassifier() {
        InstrumentedInstanceScope scope = new InstrumentedInstanceScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface1.class, instance1, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory2, Interface1.class, instance2, Classifier.NONE);
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory3, Interface1.class, instance3, Classifier.NONE);

        scope.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, never()).dispose(any());
        verify(factory3, times(1)).dispose(instance3);
    }

    @Test
    public void disposeSingleScope_WithClassifiers() {
        InstrumentedInstanceScope scope = new InstrumentedInstanceScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface1.class, instance1, "classifier1");
        scope.instrumentObjectIntoScope(factory2, Interface1.class, instance2, "classifier2");
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory3, Interface1.class, instance3, Classifier.NONE);

        scope.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, never()).dispose(any());
        verify(factory3, times(1)).dispose(instance3);
    }

    @Test
    public void disposingParentScope_DisposesChildrenScopes() {
        MagnetScope parentScope;
        InstrumentedInstanceScope parent = new InstrumentedInstanceScope(parentScope = new MagnetScope(null, instanceManager));
        when(factory1.isDisposable()).thenReturn(true);
        parent.instrumentObjectIntoScope(factory1, Interface1.class, instance1, Classifier.NONE);

        InstrumentedInstanceScope child = new InstrumentedInstanceScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface1.class, instance2, Classifier.NONE);
        child.instrumentObjectIntoScope(factory3, Interface1.class, instance3, Classifier.NONE);

        parent.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, times(1)).dispose(instance2);
        verify(factory3, never()).dispose(instance3);
    }

    @Test
    public void disposingChildScope_KeepsParentScopes() {
        MagnetScope parentScope;
        InstrumentedInstanceScope parent = new InstrumentedInstanceScope(parentScope = new MagnetScope(null, instanceManager));
        parent.instrumentObjectIntoScope(factory1, Interface1.class, instance1, Classifier.NONE);

        InstrumentedInstanceScope child = new InstrumentedInstanceScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface1.class, instance2, Classifier.NONE);

        child.dispose();

        verify(factory2, times(1)).dispose(instance2);
        verify(factory1, never()).dispose(any());
    }

    @Test(expected = IllegalStateException.class)
    public void disposedScopeThrowsException() {
        InstrumentedInstanceScope scope = new InstrumentedInstanceScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface1.class, instance1, "classifier1");

        scope.dispose();

        scope.getOptional(String.class);
    }

    @Test
    public void disposeChildScope_InReversOrdered() {
        InstrumentedInstanceScope scope = new InstrumentedInstanceScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        when(factory2.isDisposable()).thenReturn(true);
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface1.class, instance1, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory2, Interface1.class, instance2, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory3, Interface1.class, instance3, Classifier.NONE);

        scope.dispose();

        InOrder order = inOrder(factory3, factory2, factory1);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeParentScopes_InReversOrdered() {

        when(factory1.isDisposable()).thenReturn(true);
        when(factory2.isDisposable()).thenReturn(true);
        when(factory3.isDisposable()).thenReturn(true);

        MagnetScope parentScope;
        InstrumentedInstanceScope parent = new InstrumentedInstanceScope(parentScope = new MagnetScope(null, instanceManager));
        parent.instrumentObjectIntoScope(factory1, Interface1.class, instance1, Classifier.NONE);

        InstrumentedInstanceScope child = new InstrumentedInstanceScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface1.class, instance2, Classifier.NONE);

        parent.instrumentObjectIntoScope(factory3, Interface1.class, instance3, Classifier.NONE);

        parent.dispose();

        InOrder order = inOrder(factory2, factory3, factory1);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    interface Interface1 {}
    abstract static class Factory1 extends InstanceFactory<Interface1> {}
    abstract static class Factory2 extends InstanceFactory<Interface1> {}
    abstract static class Factory3 extends InstanceFactory<Interface1> {}

}
