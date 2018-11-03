package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class GenerateRegistryForScopeFactoriesTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Single scope`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Scope1.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer1.java"))

    }

    @Test
    fun `Many scopes`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Scope2_1.java"),
                withResource("Scope2_2.java"),
                withResource("Scope2_3.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer2.java"))

    }


}