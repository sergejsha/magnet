package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class SelectorTest {

    @Test
    fun `Empty selector is allowed`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation1.java")
            )

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("selector/Implementation1MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Implementation1MagnetFactory.java"))
    }

    @Test
    fun `Invalid selector fails compilation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation2.java")
            )

        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("Invalid selector")
    }

    @Test
    fun `Invalid selector id fails compilation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation3.java")
            )

        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("Invalid selector")
    }

    @Test
    fun `Invalid selector property fails compilation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation4.java")
            )

        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("Invalid selector")
    }

    @Test
    fun `Invalid selector operator fails compilation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation5.java")
            )

        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("Invalid selector")
    }

    @Test
    fun `Invalid selector operand fails compilation`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation6.java")
            )

        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("Invalid selector")
    }

    @Test
    fun `Valid selector (1 operand)`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation7.java")
            )

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("selector/Implementation7MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Implementation7MagnetFactory.java"))
    }

    @Test
    fun `Valid selector (2 operands)`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation8.java")
            )

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("selector/Implementation8MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Implementation8MagnetFactory.java"))
    }

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

}