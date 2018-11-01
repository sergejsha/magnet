package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class MagnetProcessorFactoryNamesTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Generate fully named factories for top level classes`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Delegate1.java")
            )

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Delegate1MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Delegate1MagnetFactory.java"))

    }

    @Test
    fun `Generate fully named factories for inner classes`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java")
            )

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Interface1DelegateMagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Interface1DelegateMagnetFactory.java"))

    }

}