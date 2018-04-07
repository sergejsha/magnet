package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetScopeTest {

    private Scope scope;

    @Before
    public void before() {
        scope = new MagnetScope();
    }

    @Test
    public void noClassifier_GetNotRegistered() {
        // when
        Integer dependency = scope.get(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void noClassifier_GetRegistered() {
        // given
        scope.register(Integer.class, 100);

        // when
        Integer dependency = scope.get(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noClassifier_RequireNotRegistered() {
        scope.require(Integer.class);
    }

    @Test
    public void noClassifier_RequireRegistered() {
        // given
        scope.register(Integer.class, 100);

        // when
        Integer dependency = scope.require(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noClassifier_RegisterOverwrite() {
        scope.register(Integer.class, 100);
        scope.register(Integer.class, 200);
    }

    @Test
    public void classifier_GetNotRegistered() {
        // when
        Integer dependency = scope.get(Integer.class, "common");

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void classifier_GetRegisteredNoClassifier() {
        // given
        scope.register(Integer.class, 100, "common");

        // when
        Integer dependency = scope.get(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void classifier_GetRegisteredWrongClassifier() {
        // given
        scope.register(Integer.class, 100, "common");

        // when
        Integer dependency = scope.get(Integer.class, "wrong");

        // then
        assertThat(dependency).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void classifier_RequireNotRegistered() {
        scope.require(Integer.class, "common");
    }

    @Test
    public void classifier_RequireRegistered() {
        // given
        scope.register(Integer.class, 100, "common");

        // when
        Integer dependency = scope.require(Integer.class, "common");

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void classifier_RegisterOverwrite() {
        scope.register(Integer.class, 100, "common");
        scope.register(Integer.class, 200, "common");
    }

}