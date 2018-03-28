package magnet

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
                .generatedSourceFile("app/extension/MagnetHomePageNoParamsFactory")
                .hasSourceEquivalentTo(withResource("generated/MagnetHomePageNoParamsFactory.java"))
    }

    @Test
    fun generateFactory_WithDependencyScope() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("HomePageWithDependencyScope.java"),
                        withResource("Page.java")
                )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
                .generatedSourceFile("app/extension/MagnetHomePageWithDependencyScopeFactory")
                .hasSourceEquivalentTo(withResource("generated/MagnetHomePageWithDependencyScopeFactory.java"))
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
                .generatedSourceFile("app/extension/MagnetHomePageWithParamsFactory")
                .hasSourceEquivalentTo(withResource("generated/MagnetHomePageWithParamsFactory.java"))
    }

    @Test
    fun generateFactory_WithArbitraryParamsAndDependencyScope() {

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
                .generatedSourceFile("app/extension/MagnetHomePageFactory")
                .hasSourceEquivalentTo(withResource("generated/MagnetHomePageFactory.java"))
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
    fun generateFactory_ForTypeNotImplemented() {

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
    fun generateFactoryIndex_Target() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("UserPageMenuItem.java"),
                        withResource("MenuItem.java")
                )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
                .generatedSourceFile("magnet/index/app_extension_MagnetUserPageMenuItemFactory")
                .hasSourceEquivalentTo(withResource("generated/app_extension_MagnetUserPageMenuItemFactory.java"))
    }

    @Test
    fun generateFactoryIndex_NoTarget() {

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
                .generatedSourceFile("magnet/index/app_extension_MagnetHomePageFactory")
                .hasSourceEquivalentTo(withResource("generated/app_extension_MagnetHomePageFactory.java"))
    }

    @Test
    fun generateFactoryIndex_UnknownType_SingleImpl() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("Tab.java"), // interface Tab is package private
                        withResource("UnknownTypeTab.java"),
                        withResource("AppExtensionRegistry.java")
                )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
                .generatedSourceFile("app/extension/MagnetUnknownTypeTabFactory")
                .hasSourceEquivalentTo(withResource("generated/UnknownType_MagnetUnknownTypeTabFactory.java"))

        assertThat(compilation)
                .generatedSourceFile("magnet/MagnetIndexer")
                .hasSourceEquivalentTo(withResource("generated/UnknownType_MagnetIndexer.java"))
    }

    @Test
    fun generateFactoryIndex_UnknownType_MultipleImpls() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("Tab.java"), // interface Tab is package private
                        withResource("UnknownTypeTab.java"),
                        withResource("UnknownTypeTab2.java"),
                        withResource("AppExtensionRegistry.java")
                )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
                .generatedSourceFile("app/extension/MagnetUnknownTypeTabFactory")
                .hasSourceEquivalentTo(withResource("generated/UnknownType_MagnetUnknownTypeTabFactory.java"))

        assertThat(compilation)
                .generatedSourceFile("app/extension/MagnetUnknownTypeTab2Factory")
                .hasSourceEquivalentTo(withResource("generated/UnknownType_MagnetUnknownTypeTab2Factory.java"))

        assertThat(compilation)
                .generatedSourceFile("magnet/MagnetIndexer")
                .hasSourceEquivalentTo(withResource("generated/UnknownType_MultipleImpls_MagnetIndexer.java"))
    }

    @Test
    fun generateFactoryIndex_ForInterfaceWithGenericType() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("Executor.java"),
                        withResource("ExecutorImpl.java")
                )

        assertThat(compilation).succeededWithoutWarnings()

    }

    @Test
    fun generateMagnetRegistry() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("AppExtensionRegistry.java"),
                        withResource("UserPage.java"),
                        withResource("HomePageMenuItem.java"),
                        withResource("UserPageMenuItem.java"),
                        withResource("HomePage.java"),
                        withResource("Page.java"),
                        withResource("MenuItem.java"),
                        withResource("HomeRepository.java"),
                        withResource("UserData.java")
                )

        assertThat(compilation).succeeded()

        assertThat(compilation)
                .generatedSourceFile("magnet/MagnetIndexer")
                .hasSourceEquivalentTo(withResource("generated/MagnetIndexer.java"))

    }

    @Test
    fun generateMagnetRegistry_Empty() {

        val compilation = Compiler.javac()
                .withProcessors(MagnetProcessor())
                .compile(
                        withResource("AppExtensionRegistry.java"),
                        withResource("Page.java")
                )

        assertThat(compilation).succeeded()

        assertThat(compilation)
                .generatedSourceFile("magnet/MagnetIndexer")
                .hasSourceEquivalentTo(withResource("generated/MagnetIndexer_empty.java"))

    }

    private fun withResource(name: String): JavaFileObject {
        return JavaFileObjects.forResource(javaClass.simpleName + '/' + name)
    }

}