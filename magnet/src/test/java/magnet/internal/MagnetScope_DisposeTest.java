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
    @Mock private Factory4 factory4;
    @Mock private Interface instance1;
    @Mock private Interface instance2;
    @Mock private Interface instance3;
    @Mock private Interface instance4;

    @Test
    public void disposeSingleScope_NoClassifier() {
        InstrumentedScope scope = new InstrumentedScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        scope.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, never()).dispose(any());
        verify(factory3, times(1)).dispose(instance3);
    }

    @Test
    public void disposeSingleScope_WithClassifiers() {
        InstrumentedScope scope = new InstrumentedScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface.class, instance1, "classifier1");
        scope.instrumentObjectIntoScope(factory2, Interface.class, instance2, "classifier2");
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        scope.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, never()).dispose(any());
        verify(factory3, times(1)).dispose(instance3);
    }

    @Test
    public void disposingParentScope_DisposesChildrenScopes() {
        MagnetScope parentScope;
        InstrumentedScope parent = new InstrumentedScope(parentScope = new MagnetScope(null, instanceManager));
        when(factory1.isDisposable()).thenReturn(true);
        parent.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        InstrumentedScope child = new InstrumentedScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);
        child.instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        parent.dispose();

        verify(factory1, times(1)).dispose(instance1);
        verify(factory2, times(1)).dispose(instance2);
        verify(factory3, never()).dispose(instance3);
    }

    @Test
    public void disposingChildScope_KeepsParentScopes() {
        MagnetScope parentScope;
        InstrumentedScope parent = new InstrumentedScope(parentScope = new MagnetScope(null, instanceManager));
        parent.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        InstrumentedScope child = new InstrumentedScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);

        child.dispose();

        verify(factory2, times(1)).dispose(instance2);
        verify(factory1, never()).dispose(any());
    }

    @Test(expected = IllegalStateException.class)
    public void disposedScopeThrowsException() {
        InstrumentedScope scope = new InstrumentedScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface.class, instance1, "classifier1");

        scope.dispose();

        scope.getOptional(String.class);
    }

    @Test
    public void disposeChildScope_InReversOrdered() {
        InstrumentedScope scope = new InstrumentedScope(
            new MagnetScope(null, instanceManager)
        );
        when(factory1.isDisposable()).thenReturn(true);
        when(factory2.isDisposable()).thenReturn(true);
        when(factory3.isDisposable()).thenReturn(true);
        scope.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);
        scope.instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        scope.dispose();

        InOrder order = inOrder(factory3, factory2, factory1);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeParentScope_InReversOrdered() {

        when(factory1.isDisposable()).thenReturn(true);
        when(factory2.isDisposable()).thenReturn(true);
        when(factory3.isDisposable()).thenReturn(true);

        MagnetScope parentScope;
        InstrumentedScope parent = new InstrumentedScope(parentScope = new MagnetScope(null, instanceManager));
        parent.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        InstrumentedScope child = new InstrumentedScope((MagnetScope) parentScope.createSubscope());
        when(factory2.isDisposable()).thenReturn(true);
        child.instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);

        parent.instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        parent.dispose();

        InOrder order = inOrder(factory2, factory3, factory1);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeFirstChildThenParentScopes() {

        when(factory1.isDisposable()).thenReturn(true);
        InstrumentedScope parent =
            new InstrumentedScope(new MagnetScope(null, instanceManager))
                .instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        when(factory2.isDisposable()).thenReturn(true);
        InstrumentedScope child =
            parent.createInstrumentedSubscope()
                .instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);

        child.dispose();
        parent.dispose();

        InOrder order = inOrder(factory2, factory1);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_123() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[1].dispose();
        child[2].dispose();
        child[3].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory2, factory3, factory4, factory1);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_132() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[1].dispose();
        child[3].dispose();
        child[2].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory2, factory4, factory3, factory1);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_213() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[2].dispose();
        child[1].dispose();
        child[3].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory3, factory2, factory4, factory1);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_231() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[2].dispose();
        child[3].dispose();
        child[1].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory3, factory4, factory2, factory1);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_312() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[3].dispose();
        child[1].dispose();
        child[2].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory4, factory2, factory3, factory1);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    @Test
    public void disposeMultipleChildScopes_321() {

        InstrumentedScope[] child = prepareMultipleChildrenScopes();

        child[3].dispose();
        child[2].dispose();
        child[1].dispose();
        child[0].dispose();

        InOrder order = inOrder(factory4, factory3, factory2, factory1);
        order.verify(factory4, times(1)).dispose(instance4);
        order.verify(factory3, times(1)).dispose(instance3);
        order.verify(factory2, times(1)).dispose(instance2);
        order.verify(factory1, times(1)).dispose(instance1);
    }

    private InstrumentedScope[] prepareMultipleChildrenScopes() {

        when(factory1.isDisposable()).thenReturn(true);
        InstrumentedScope parent =
            new InstrumentedScope(new MagnetScope(null, instanceManager))
                .instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        when(factory2.isDisposable()).thenReturn(true);
        InstrumentedScope child1 =
            parent.createInstrumentedSubscope()
                .instrumentObjectIntoScope(factory2, Interface.class, instance2, Classifier.NONE);

        when(factory3.isDisposable()).thenReturn(true);
        InstrumentedScope child2 =
            parent.createInstrumentedSubscope()
                .instrumentObjectIntoScope(factory3, Interface.class, instance3, Classifier.NONE);

        when(factory4.isDisposable()).thenReturn(true);
        InstrumentedScope child3 =
            parent.createInstrumentedSubscope()
                .instrumentObjectIntoScope(factory4, Interface.class, instance4, Classifier.NONE);

        return new InstrumentedScope[]{parent, child1, child2, child3};

    }

    interface Interface {}
    abstract static class Factory1 extends InstanceFactory<Interface> {}
    abstract static class Factory2 extends InstanceFactory<Interface> {}
    abstract static class Factory3 extends InstanceFactory<Interface> {}
    abstract static class Factory4 extends InstanceFactory<Interface> {}

}
