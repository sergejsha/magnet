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

package magnet

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@Suppress("UNUSED_VARIABLE")
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class MagnetTest {

    @Mock
    lateinit var implManager: ImplementationManager

    @Mock
    lateinit var scope: Scope

    @Test
    fun testGetMany() {
        // when
        val many: List<String> = implManager.getMany<String>("target", scope)

        // then
        verify(implManager).getMany(String::class.java, "target", scope)
    }

    @Test
    fun testGetSingle() {
        // when
        val single: String? = implManager.getSingle<String>("target", scope)

        // then
        verify(implManager).getSingle(String::class.java, "target", scope)
    }

    @Test
    fun testRequireSingle() {
        // when
        val single: String = implManager.requireSingle<String>("target", scope)

        // then
        verify(implManager).requireSingle(String::class.java, "target", scope)
    }

}