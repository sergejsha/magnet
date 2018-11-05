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
    fun `Getters in single interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope1.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetScope1Implementation")
            .hasSourceEquivalentTo(withResource("expected/MagnetScope1Implementation.java"))

    }

    @Test
    fun `Binders in single interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope2.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetScope2Implementation")
            .hasSourceEquivalentTo(withResource("expected/MagnetScope2Implementation.java"))

    }

    @Test
    fun `Bind ParentScope with @Scope annotation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope3_1.java"),
                withResource("Scope3.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetScope3Implementation")
            .hasSourceEquivalentTo(withResource("expected/MagnetScope3Implementation.java"))

    }

    @Test
    fun `Bind ParentScope without @Scope annotation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope7_1.java"),
                withResource("Scope7.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetScope7Implementation")
            .hasSourceEquivalentTo(withResource("expected/MagnetScope7Implementation.java"))

    }

    @Test
    fun `Scope must be interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope4.java")
            )
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("interface")

    }

    @Test
    fun `Getters in inherited interfaces`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope5_1.java"),
                withResource("Scope5_2.java"),
                withResource("Scope5_3.java"),
                withResource("Scope5.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetScope5Implementation")
            .hasSourceEquivalentTo(withResource("expected/MagnetScope5Implementation.java"))

    }

    @Test
    fun `Scope cannot inherit from another scope`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope6.java"),
                withResource("Scope6_1.java")
            )
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("cannot inherit")

    }

}