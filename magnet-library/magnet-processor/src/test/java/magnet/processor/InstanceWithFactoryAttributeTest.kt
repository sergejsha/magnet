package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class InstanceWithFactoryAttributeTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Custom factory gets called`() {
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

}