package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class InstanceCustomFactoryProcessorTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Not parametrized custom Factory`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Implementation1.java"),
                withResource("CustomFactory1.java")
            )
        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Implementation1MagnetFactory")
            .hasSourceEquivalentTo(withResource("expected/Implementation1MagnetFactory.java"))
    }

    @Test
    fun `Parametrized custom Factory`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface2.java"),
                withResource("Implementation2.java"),
                withResource("CustomFactory2.java")
            )
        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Implementation2MagnetFactory")
            .hasSourceEquivalentTo(withResource("expected/Implementation2MagnetFactory.java"))
    }

    @Test
    fun `Custom Factory for implementation with dependencies`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface3.java"),
                withResource("Implementation3.java"),
                withResource("CustomFactory3.java")
            )
        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Implementation3MagnetFactory")
            .hasSourceEquivalentTo(withResource("expected/Implementation3MagnetFactory.java"))
    }
}