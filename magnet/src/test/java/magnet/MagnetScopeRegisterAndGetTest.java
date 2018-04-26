package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetScopeRegisterAndGetTest {

    @Mock
    InstanceManager instanceManager;

    private Scope scope;

    @Before
    public void before() {
        scope = new MagnetScope(null, instanceManager);
    }

    @Test
    public void noClassifier_GetOptionalNotRegistered() {
        // when
        Integer dependency = scope.getOptional(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void noClassifier_GetOptionalRegistered() {
        // given
        scope.bind(Integer.class, 100);

        // when
        Integer dependency = scope.getOptional(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noClassifier_GetSingleNotRegistered() {
        scope.getSingle(Integer.class);
    }

    @Test
    public void noClassifier_GetSingleRegistered() {
        // given
        scope.bind(Integer.class, 100);

        // when
        Integer dependency = scope.getSingle(Integer.class);

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void noClassifier_RegisterOverwrite() {
        scope.bind(Integer.class, 100);
        scope.bind(Integer.class, 200);
    }

    @Test
    public void classifier_GetOptionalNotRegistered() {
        // when
        Integer dependency = scope.getOptional(Integer.class, "common");

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void classifier_GetOptionalRegisteredNoClassifier() {
        // given
        scope.bind(Integer.class, 100, "common");

        // when
        Integer dependency = scope.getOptional(Integer.class);

        // then
        assertThat(dependency).isNull();
    }

    @Test
    public void classifier_GetOptionalRegisteredWrongClassifier() {
        // given
        scope.bind(Integer.class, 100, "common");

        // when
        Integer dependency = scope.getOptional(Integer.class, "wrong");

        // then
        assertThat(dependency).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void classifier_GetSingleNotRegistered() {
        scope.getSingle(Integer.class, "common");
    }

    @Test
    public void classifier_GetSingleRegistered() {
        // given
        scope.bind(Integer.class, 100, "common");

        // when
        Integer dependency = scope.getSingle(Integer.class, "common");

        // then
        assertThat(dependency).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void classifier_RegisterOverwrite() {
        scope.bind(Integer.class, 100, "common");
        scope.bind(Integer.class, 200, "common");
    }

}