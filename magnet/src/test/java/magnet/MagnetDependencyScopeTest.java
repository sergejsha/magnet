package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetDependencyScopeTest {

    private DependencyScope dependencyScope;

    @Before
    public void before() {
        dependencyScope = new MagnetDependencyScope();
    }

    @Test
    public void noQualifier_GetNotRegistered() {
        // when
        Integer dependency = dependencyScope.get(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void noQualifier_GetRegistered() {
        // given
        dependencyScope.register(Integer.class, 100);

        // when
        Integer dependency = dependencyScope.get(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noQualifier_RequireNotRegistered() {
        dependencyScope.require(Integer.class);
    }

    @Test
    public void noQualifier_RequireRegistered() {
        // given
        dependencyScope.register(Integer.class, 100);

        // when
        Integer dependency = dependencyScope.require(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noQualifier_RegisterOverwrite() {
        dependencyScope.register(Integer.class, 100);
        dependencyScope.register(Integer.class, 200);
    }

    @Test
    public void qualifier_GetNotRegistered() {
        // when
        Integer dependency = dependencyScope.get(Integer.class, "common");

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void qualifier_GetRegisteredNoQualifier() {
        // given
        dependencyScope.register(Integer.class, 100, "common");

        // when
        Integer dependency = dependencyScope.get(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void qualifier_GetRegisteredWrongQualifier() {
        // given
        dependencyScope.register(Integer.class, 100, "common");

        // when
        Integer dependency = dependencyScope.get(Integer.class, "wrong");

        // then
        assertThat(dependency).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void qualifier_RequireNotRegistered() {
        dependencyScope.require(Integer.class, "common");
    }

    @Test
    public void qualifier_RequireRegistered() {
        // given
        dependencyScope.register(Integer.class, 100, "common");

        // when
        Integer dependency = dependencyScope.require(Integer.class, "common");

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void qualifier_RegisterOverwrite() {
        dependencyScope.register(Integer.class, 100, "common");
        dependencyScope.register(Integer.class, 200, "common");
    }

}