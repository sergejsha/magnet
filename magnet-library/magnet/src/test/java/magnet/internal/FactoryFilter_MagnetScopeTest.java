package magnet.internal;

import magnet.SelectorFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FactoryFilter_MagnetScopeTest {

    private InstrumentedInstanceScope scope;

    @Mock InstanceManager instanceManager;
    @Mock InstanceFactory instanceFactory;
    @Mock SelectorFilter selectorFilter;

    @Before
    public void before() {
        scope = new InstrumentedInstanceScope(new MagnetScopeContainer(null, instanceManager));
    }

    @Test
    public void noSelectorReturnsTrue() {
        when(instanceFactory.getSelector()).thenReturn(null);
        boolean result = scope.filter(instanceFactory);
        assertThat(result).isTrue();
    }

    @Test(expected = IllegalStateException.class)
    public void missingSelectorFactoryThrows() {
        when(instanceFactory.getSelector()).thenReturn(new String[]{"test"});
        scope.filter(instanceFactory);
    }

    @Test
    public void existingSelectorReturnsFilterTrue() {
        scope.bind(SelectorFilter.class, selectorFilter, "test");
        when(instanceFactory.getSelector()).thenReturn(new String[]{"test"});
        when(selectorFilter.filter(any())).thenReturn(true);
        boolean result = scope.filter(instanceFactory);
        assertThat(result).isTrue();
    }

    @Test
    public void existingSelectorReturnsFilterFalse() {
        scope.bind(SelectorFilter.class, selectorFilter, "test");
        when(instanceFactory.getSelector()).thenReturn(new String[]{"test"});
        when(selectorFilter.filter(any())).thenReturn(false);
        boolean result = scope.filter(instanceFactory);
        assertThat(result).isFalse();
    }

}
