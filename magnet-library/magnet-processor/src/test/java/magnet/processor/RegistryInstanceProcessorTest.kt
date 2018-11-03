package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class RegistryInstanceProcessorTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `No factories`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer2.java"))


    }

    @Test
    fun `Single factory`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface1.java"),
                withResource("Implementation1.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer1.java"))

    }

    @Test
    fun `Single factory, inner implementation`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface7.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer7.java"))

    }

    @Test
    fun `Many factories, same type`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface3.java"),
                withResource("Implementation3_1.java"),
                withResource("Implementation3_2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer3.java"))

    }

    @Test
    fun `Many factories, same type, same classifier`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface5.java"),
                withResource("Implementation5_1.java"),
                withResource("Implementation5_2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer5.java"))

    }

    @Test
    fun `Many factories, same type, different classifiers`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface4.java"),
                withResource("Implementation4_1.java"),
                withResource("Implementation4_2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer4.java"))

    }

    @Test
    fun `Many factories, different types`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("App.java"),
                withResource("Interface6_1.java"),
                withResource("Interface6_2.java"),
                withResource("Implementation6_1.java"),
                withResource("Implementation6_2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("expected/MagnetIndexer6.java"))

    }


}