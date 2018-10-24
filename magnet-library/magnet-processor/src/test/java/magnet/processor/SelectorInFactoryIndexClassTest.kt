package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class SelectorInFactoryIndexClassTest {

    @Test
    fun `Empty factories generates generation code with null`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Application1.java"),
                withResource("Implementation1.java"),
                withResource("Implementation2.java"),
                withResource("Interface1.java"),
                withResource("Interface2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("generated/MagnetIndex1.java"))

    }

    @Test
    fun `Demo factory generates initialization code`() {

        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Application2.java"),
                withResource("SimpleSelectorFilter.java"),
                withResource("Implementation1.java"),
                withResource("Implementation2.java"),
                withResource("Interface1.java"),
                withResource("Interface2.java")
            )

        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet/internal/MagnetIndexer")
            .hasSourceEquivalentTo(withResource("generated/MagnetIndex2.java"))

    }

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

}