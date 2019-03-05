package magnet.processor

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class MagnetProcessorTest {

    @Test
    fun generateFactory_NoParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageNoParams.java"),
                withResource("Page.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageNoParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageNoParamsMagnetFactory.java"))
    }

    @Test
    fun generateFactory_WithScope() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithScope.java"),
                withResource("Page.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithScopeMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithScopeMagnetFactory.java"))
    }

    @Test
    fun generateFactory_WithArbitraryParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithParams.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java"),
                withResource("UserData.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithParamsMagnetFactory.java"))
    }

    @Test
    fun generateFactory_WithArbitraryParamsAndScope() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePage.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java"),
                withResource("UserData.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageMagnetFactory.java"))
    }

    @Test
    fun generateFactory_FailsOnGenericTypeInConstructorParameter() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithGenericParam.java"),
                withResource("Page.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("is specified using a generic type")
    }

    @Test
    fun generateFactory_TypeNotImplemented() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Tab.java"),
                withResource("UnimplementedTab.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must implement")
    }

    @Test
    fun generateFactory_DisabledAnnotation() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Tab.java"),
                withResource("DisabledTab.java")
            )

        assertThat(compilation).succeeded()
        com.google.common.truth.Truth.assertThat(compilation.generatedFiles().size).isEqualTo(2)

    }

    @Test
    fun generateFactory_WithClassifierParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithClassifierParams.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java"),
                withResource("UserData.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithClassifierParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithClassifierParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyParams.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyParameterizedParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyParameterizedParams.java"),
                withResource("Page.java"),
                withResource("WorkProcessor.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyParameterizedParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyParameterizedParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyParameterizedWildcardOutParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyParameterizedWildcardOutParams.java"),
                withResource("Page.java"),
                withResource("WorkProcessor.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyParameterizedWildcardOutParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyParameterizedWildcardOutParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyParameterizedWildcardInParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyParameterizedWildcardInParams.java"),
                withResource("Page.java"),
                withResource("WorkProcessor.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyParameterizedWildcardInParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyParameterizedWildcardInParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyParameterizedWildcardKnownParams() {

        // This is what Kotlin provides to annotation processor, when Kotlin generics are used as parameters

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyParameterizedWildcardKnownParams.java"),
                withResource("Page.java"),
                withResource("WorkProcessor.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyParameterizedWildcardKnownParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyParameterizedWildcardKnownParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_WithManyWildcardParams() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithManyWildcardParams.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageWithManyWildcardParamsMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithManyWildcardParamsMagnetFactory.java"))

    }

    @Test
    fun generateFactory_UsingStaticMethod() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithStaticConstructor.java"),
                withResource("HomePageWithStaticConstructorSingle.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/utils/HomePageWithStaticConstructorSingleCreateRepositoriesMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageWithStaticConstructorSingleCreateRepositoriesMagnetFactory.java"))

    }

    @Test
    fun generateFactory_StaticMethodProvidesInnerClass() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("StaticMethodProvidesInnerClass/PowerManager.java"),
                withResource("StaticMethodProvidesInnerClass/PowerManagerProvider.java")
            )

        assertThat(compilation).succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("app/PowerManagerProviderProvideWakeLockMagnetFactory")
            .hasSourceEquivalentTo(withResource("StaticMethodProvidesInnerClass/generated/PowerManagerProviderProvideWakeLockMagnetFactory.java"))
    }

    @Test
    fun generateFactory_SingleLazyParameter() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("SingleLazyParameter/UnderTest.java")
            )

        assertThat(compilation).succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("app/UnderTest.java")
            .hasSourceEquivalentTo(withResource("SingleLazyParameter/generated/UnderTestMagnetFactory.java"))
    }

    @Test
    fun generateFactory_DisabledAnnotation_UsingStaticMethod() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageWithStaticConstructor.java"),
                withResource("HomePageWithStaticConstructorDisabled.java"),
                withResource("Page.java"),
                withResource("HomeRepository.java")
            )

        assertThat(compilation).succeededWithoutWarnings()
        com.google.common.truth.Truth.assertThat(compilation.generatedFiles().size).isEqualTo(4)

    }

    @Test
    fun generateFactoryIndex_ForInterfaceWithGenericType() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Executor.java"),
                withResource("ExecutorImpl.java"),
                withResource("AppExtensionRegistry.java")
            )

        assertThat(compilation)
            .generatedSourceFile("app/extension/ExecutorImplMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/ForInterfaceWithGenericType_ExecutorMagnetFactory.java"))

    }

    @Test
    fun generateFactoryIndex_DependentOnInterfaceWithGenericType() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Executor.java"),
                withResource("ExecutorImpl.java"),
                withResource("ExecutorMaster.java"),
                withResource("AppExtensionRegistry.java")
            )

        assertThat(compilation)
            .generatedSourceFile("app/extension/ExecutorMasterMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/DependentOnInterfaceWithGenericType_ExecutorMagnetFactory.java"))

    }

    @Test
    fun generateFactory_FailOnAdditionalPackageProtectedConstructor() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageConstructorsWithAdditionalPackageProtected.java"),
                withResource("Page.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must have exactly one public or package-protected constructor")
    }

    @Test
    fun generateFactory_FailWithNoPublicOrPackageProtectedConstructor() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageConstructorsWithNoPublicOrPackageProtected.java"),
                withResource("Page.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must have exactly one public or package-protected constructor")
    }

    @Test
    fun generateFactory_AllowAdditionalPrivateConstructor() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageConstructorsWithAdditionalPrivate.java"),
                withResource("Page.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageConstructorsWithAdditionalPrivateMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageConstructorsWithAdditionalPrivateMagnetFactory.java"))
    }

    @Test
    fun generateFactory_AllowAdditionalProtectedConstructor() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("HomePageConstructorsWithAdditionalProtected.java"),
                withResource("Page.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("app/extension/HomePageConstructorsWithAdditionalProtectedMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/HomePageConstructorsWithAdditionalProtectedMagnetFactory.java"))
    }

    private fun withResource(name: String): JavaFileObject {
        return JavaFileObjects.forResource(javaClass.simpleName + '/' + name)
    }

}
