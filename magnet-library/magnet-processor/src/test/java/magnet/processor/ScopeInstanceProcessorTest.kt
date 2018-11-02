package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class ScopeInstanceProcessorTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Getters get generated`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope1.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope1")
            .hasSourceEquivalentTo(withResource("generated/MagnetInstanceScope1.java"))

    }

    @Test
    fun `Binders get generated`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope2.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope2")
            .hasSourceEquivalentTo(withResource("generated/MagnetInstanceScope2.java"))

    }

    @Test
    fun `Parent Scope binder gets generated`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("ParentScope.java"),
                withResource("Scope3.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope3")
            .hasSourceEquivalentTo(withResource("generated/MagnetInstanceScope3.java"))

    }

}