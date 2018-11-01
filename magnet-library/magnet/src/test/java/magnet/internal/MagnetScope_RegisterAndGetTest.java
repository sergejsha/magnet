/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnet.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetScope_RegisterAndGetTest {

    @Mock
    InstanceManager instanceManager;

    private ScopeContainer scope;

    @Before
    public void before() {
        scope = new MagnetScopeContainer(null, instanceManager);
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
