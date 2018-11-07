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

package magnet.processor.registry

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.CodeWriter
import magnet.processor.registry.instances.InstanceIndexGenerator
import javax.lang.model.element.Modifier

private const val INSTANCE_MANAGER = "instanceManager"
private const val INSTANCE_MANAGER_NAME = "MagnetInstanceManager"
private const val INSTANCE_MANAGER_PACKAGE = "magnet.internal"

class RegistryGenerator {

    private val instanceIndexGenerator = InstanceIndexGenerator()

    fun generate(registry: Model.Registry): CodeWriter {

        val instanceFactoriesIndex = instanceIndexGenerator.generate(registry)

        val registryClassName = ClassName.bestGuess(REGISTRY_CLASS_NAME)
        val factoryRegistryClassName = ClassName.get(INSTANCE_MANAGER_PACKAGE, INSTANCE_MANAGER_NAME)

        val typeSpec = TypeSpec
            .classBuilder(registryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(MethodSpec
                .methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec
                    .builder(factoryRegistryClassName, INSTANCE_MANAGER)
                    .build())
                .addCode(instanceFactoriesIndex)
                .addStatement("\$L.register(factories, index)", INSTANCE_MANAGER)
                .build())
            .build()

        val packageName = registryClassName.packageName()
        return CodeWriter(packageName, typeSpec)
    }

}
